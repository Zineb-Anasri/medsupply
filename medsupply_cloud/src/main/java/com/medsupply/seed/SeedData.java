package com.medsupply.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.medsupply.utils.SupabaseClient;

/**
 * SeedData — standalone demo-data generator for MedSupply Cloud, written against the REAL
 * Supabase schema (Moroccan context, MAD). ~80% of the generated data centers on the HOSPITAL
 * client {@code anzineb3@medsupply.dz}; the rest is an admin, three suppliers (with a little
 * inventory) and four other clients.
 *
 * <p>Reuses {@link SupabaseClient} (loads the Supabase URL + key from {@code supabase.properties} /
 * env, exactly like the app) and BCrypt for password hashing. It REUSES the catalogue
 * (categories / brands / products) already present in the database for the buying pool.
 *
 * <p>Run:  {@code mvn -q compile exec:java -Dexec.mainClass=com.medsupply.seed.SeedData}
 *          (pass exec.args "append" to skip the wipe).
 *
 * <p>By default it WIPES the seeded demo accounts (resolved by their fixed emails / ownership) and
 * all their child rows first, so it is safe to re-run. It never touches data outside those accounts.
 * Tables that do not exist in this database (tender_items, supplier_bids, maintenance_requests) are
 * intentionally not used.
 */
public class SeedData {

    // ---- Demo logins -----------------------------------------------------------
    static final String ADMIN_EMAIL = "admin@medsupply.dz";
    static final String ADMIN_PW = "Admin@1234";
    static final String ANZINEB_EMAIL = "anzineb3@medsupply.dz";
    static final String ANZINEB_PW = "Demo@1234";
    static final String SUPPLIER_PW = "Supplier@1234";
    static final String CLIENT_PW = "Client@1234";

    // ---- Shared state ----------------------------------------------------------
    static final Random RND = new Random(20260616L);
    static final LocalDateTime NOW = LocalDateTime.now();
    static final Map<String, Integer> COUNTS = new TreeMap<>();

    static final double RABAT_LAT = 34.0209;
    static final double RABAT_LON = -6.8417;

    static String anzinebUserId;
    static String anzinebClientId;
    static final List<Prod> pool = new ArrayList<>();   // buying pool (existing + new products)
    static List<String> categoryIds = new ArrayList<>();
    static List<String> brandIds = new ArrayList<>();
    static int quoteSeq = 1000;
    static int orderSeq = 1000;

    record Prod(String id, BigDecimal unitPrice, int warrantyMonths) {
        boolean isEquipment() { return warrantyMonths > 0 && unitPrice.compareTo(new BigDecimal("10000")) >= 0; }
    }
    record Line(Prod prod, int qty) {
        BigDecimal lineTotal() { return prod.unitPrice().multiply(BigDecimal.valueOf(qty)); }
    }
    record OrderRec(String orderId, BigDecimal total, LocalDateTime orderDate,
                    String status, String paymentStatus, List<Line> lines) {}

    public static void main(String[] args) {
        List<String> argv = Arrays.asList(args);
        boolean append = argv.contains("--append") || argv.contains("append");
        try {
            System.out.println("=========================================");
            System.out.println(" MedSupply Cloud — demo data seeder");
            System.out.println("=========================================");
            if (!append) {
                System.out.println("[seed] Wiping previously-seeded demo data...");
                wipe();
            } else {
                System.out.println("[seed] 'append' given: skipping wipe (may create duplicates).");
            }
            seed();
            printSummary();
            verify();
            printLogins();
            System.out.println("[seed] Done.");
        } catch (Exception e) {
            System.err.println("[seed] FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // =====================================================================================
    //  SEED
    // =====================================================================================
    static void seed() throws Exception {
        // 1) Reuse existing reference catalogue ------------------------------------------
        categoryIds = ids(getRows("categories", "select=id&order=id"), "id");
        brandIds = ids(getRows("brands", "select=id&order=id"), "id");
        loadExistingProducts();
        System.out.println("[seed] reusing " + categoryIds.size() + " categories, " + brandIds.size()
                + " brands, " + pool.size() + " existing products");

        // 2) Admin -----------------------------------------------------------------------
        createUser(ADMIN_EMAIL, ADMIN_PW, "ADMIN", NOW.minusDays(300));

        // 3) Suppliers (users + profiles) + a little inventory ---------------------------
        List<String> supplierProfileIds = new ArrayList<>();
        for (String[] s : SeedCatalog.SUPPLIERS) {
            String uid = createUser(s[2], SUPPLIER_PW, "SUPPLIER", NOW.minusDays(260));
            JsonObject body = new JsonObject();
            body.addProperty("user_id", uid);
            body.addProperty("company_name", s[0]);
            body.addProperty("contact_name", s[1]);
            body.addProperty("email", s[2]);
            body.addProperty("phone", s[3]);
            body.addProperty("address", s[4] + ", " + s[5]);
            body.addProperty("is_active", true);
            body.addProperty("created_at", iso(NOW.minusDays(260)));
            supplierProfileIds.add(insert("suppliers", body));
        }
        for (String[] p : SeedCatalog.NEW_PRODUCTS) {
            int catIdx = Integer.parseInt(p[5]);
            int brandIdx = Integer.parseInt(p[6]);
            int supIdx = Integer.parseInt(p[7]);
            BigDecimal price = new BigDecimal(p[3]);
            int warranty = Integer.parseInt(p[4]);
            JsonObject body = new JsonObject();
            body.addProperty("supplier_id", supplierProfileIds.get(supIdx));
            if (!categoryIds.isEmpty()) body.addProperty("category_id", categoryIds.get(catIdx % categoryIds.size()));
            if (!brandIds.isEmpty()) body.addProperty("brand_id", brandIds.get(brandIdx % brandIds.size()));
            body.addProperty("name", p[0]);
            body.addProperty("reference", p[1] + "-S" + supIdx);
            body.addProperty("description", p[2]);
            body.addProperty("unit_price", price);
            body.addProperty("warranty_months", warranty);
            body.addProperty("image_url", "https://picsum.photos/seed/" + p[1] + "/600/400");
            body.addProperty("is_active", true);
            body.addProperty("created_at", iso(NOW.minusDays(255)));
            String pid = insert("products", body);
            pool.add(new Prod(pid, price, warranty));
        }
        System.out.println("[seed] " + supplierProfileIds.size() + " suppliers (+"
                + SeedCatalog.NEW_PRODUCTS.length + " products) created");

        // 4) anzineb3 (HOSPITAL) — the star account --------------------------------------
        anzinebUserId = createUser(ANZINEB_EMAIL, ANZINEB_PW, "CLIENT", NOW.minusDays(240));
        anzinebClientId = createClient(anzinebUserId, "Hôpital Universitaire Ibn Sina", "HOSPITAL",
                "Dr. Anis Zineb", "0537 77 11 22", "Avenue Ibn Sina, Agdal", "Rabat", "ICE002847913",
                true, 60, NOW.minusDays(240));
        System.out.println("[seed] anzineb3 (HOSPITAL) created");

        seedAnzinebGraph();

        // 5) Other clients (the ~20%) ----------------------------------------------------
        for (String[] c : SeedCatalog.OTHER_CLIENTS) seedOtherClient(c);
        System.out.println("[seed] other clients created");
    }

    static void loadExistingProducts() throws Exception {
        JsonArray rows = getRows("products", "select=id,unit_price,warranty_months&is_active=eq.true");
        for (JsonElement el : rows) {
            JsonObject o = el.getAsJsonObject();
            if (!o.has("id") || o.get("id").isJsonNull()) continue;
            BigDecimal price = o.has("unit_price") && !o.get("unit_price").isJsonNull()
                    ? o.get("unit_price").getAsBigDecimal() : new BigDecimal("1000");
            int warranty = o.has("warranty_months") && !o.get("warranty_months").isJsonNull()
                    ? o.get("warranty_months").getAsInt() : 0;
            pool.add(new Prod(o.get("id").getAsString(), price, warranty));
        }
    }

    /** anzineb3's quotes, orders, payments, deliveries, maintenance, tenders, notifications. */
    static void seedAnzinebGraph() throws Exception {
        int creditDays = 60;
        // {orderStatus, paymentStatus, deliveryStatus|null, daysAgo}
        String[][] scenarios = {
            {"DELIVERED",  "PAID",    "DELIVERED",  "230"},
            {"DELIVERED",  "PAID",    "DELIVERED",  "200"},
            {"DELIVERED",  "PARTIAL", "DELIVERED",  "175"},
            {"DELIVERED",  "PAID",    "DELIVERED",  "130"},
            {"SHIPPED",    "PARTIAL", "IN_TRANSIT", "40"},
            {"SHIPPED",    "PAID",    "IN_TRANSIT", "30"},
            {"SHIPPED",    "UNPAID",  "IN_TRANSIT", "22"},
            {"PREPARING",  "PARTIAL", "PREPARING",  "15"},
            {"PREPARING",  "UNPAID",  "PREPARING",  "12"},
            {"CONFIRMED",  "UNPAID",  null,         "8"},
            {"PENDING",    "UNPAID",  null,         "5"},
            {"CONFIRMED",  "PAID",    null,         "60"},
        };
        List<OrderRec> orders = new ArrayList<>();
        for (String[] sc : scenarios) {
            LocalDateTime orderDate = NOW.minusDays(Long.parseLong(sc[3])).withHour(10).withMinute(15).withSecond(0);
            orders.add(buildOrderChain(anzinebClientId, creditDays, orderDate, sc[0], sc[1], sc[2],
                    "DELIVERED".equals(sc[0])));
        }
        System.out.println("[seed]   anzineb3: " + orders.size() + " orders (quotes/payments/deliveries)");

        // Standalone quotes across the real quote statuses.
        seedStandaloneQuote(anzinebClientId, "PENDING",    NOW.minusDays(18));
        seedStandaloneQuote(anzinebClientId, "PENDING",    NOW.minusDays(9));
        seedStandaloneQuote(anzinebClientId, "PROCESSING", NOW.minusDays(26));
        seedStandaloneQuote(anzinebClientId, "PROCESSING", NOW.minusDays(16));
        seedStandaloneQuote(anzinebClientId, "VALIDATED",  NOW.minusDays(33));
        seedStandaloneQuote(anzinebClientId, "REJECTED",   NOW.minusDays(50));

        seedMaintenance(orders);
        seedTenders();
        seedAnzinebNotifications(orders);
    }

    static OrderRec buildOrderChain(String clientId, int creditDays, LocalDateTime orderDate,
                                    String orderStatus, String paymentStatus, String deliveryStatus,
                                    boolean ensureEquipment) throws Exception {
        List<Line> lines = pickLines(ensureEquipment);
        BigDecimal total = BigDecimal.ZERO;
        for (Line l : lines) total = total.add(l.lineTotal());
        total = scale(total);

        // Backing quote (VALIDATED) ----------------------------------------------------
        LocalDateTime quoteDate = orderDate.minusDays(4 + RND.nextInt(7));
        JsonObject q = new JsonObject();
        q.addProperty("client_id", clientId);
        q.addProperty("status", "VALIDATED");
        q.addProperty("total_amount", total);
        q.addProperty("notes", "Devis validé et converti en commande.");
        q.addProperty("quote_number", "DEV-2026-" + (quoteSeq++));
        q.addProperty("created_at", iso(quoteDate));
        q.addProperty("updated_at", iso(orderDate));
        String quoteId = insert("quotes", q);
        for (Line l : lines) addItem("quote_items", "quote_id", quoteId, l);

        // Order + items ----------------------------------------------------------------
        JsonObject o = new JsonObject();
        o.addProperty("client_id", clientId);
        o.addProperty("quote_id", quoteId);
        o.addProperty("status", orderStatus);
        o.addProperty("payment_status", paymentStatus);
        o.addProperty("total_amount", total);
        o.addProperty("due_date", orderDate.toLocalDate().plusDays(creditDays).toString());
        o.addProperty("order_number", "CMD-2026-" + (orderSeq++));
        o.addProperty("created_at", iso(orderDate));
        String orderId = insert("orders", o);
        for (Line l : lines) addItem("order_items", "order_id", orderId, l);

        // Payments (transactions) — reflect payment_status ------------------------------
        buildPayments(orderId, total, orderDate, paymentStatus);

        // Delivery ---------------------------------------------------------------------
        if (deliveryStatus != null) buildDelivery(orderId, deliveryStatus, orderDate);

        return new OrderRec(orderId, total, orderDate, orderStatus, paymentStatus, lines);
    }

    static void addItem(String table, String fkCol, String fkId, Line l) throws Exception {
        JsonObject it = new JsonObject();
        it.addProperty(fkCol, fkId);
        it.addProperty("product_id", l.prod().id());
        it.addProperty("quantity", l.qty());
        it.addProperty("unit_price", l.prod().unitPrice());
        insert(table, it);
    }

    static void buildPayments(String orderId, BigDecimal total, LocalDateTime orderDate, String paymentStatus)
            throws Exception {
        if ("UNPAID".equals(paymentStatus)) return;
        BigDecimal toPay = "PARTIAL".equals(paymentStatus)
                ? scale(total.multiply(new BigDecimal("0.40").add(new BigDecimal("0.20").multiply(BigDecimal.valueOf(RND.nextDouble())))))
                : total;
        // PAID: occasionally split into two installments.
        boolean split = "PAID".equals(paymentStatus) && total.compareTo(new BigDecimal("50000")) > 0 && RND.nextBoolean();
        if (split) {
            BigDecimal first = scale(total.multiply(new BigDecimal("0.5")));
            insertPayment(orderId, first, orderDate.plusDays(3), "Premier versement");
            insertPayment(orderId, scale(total.subtract(first)), orderDate.plusDays(20), "Solde");
        } else {
            insertPayment(orderId, toPay, orderDate.plusDays(2 + RND.nextInt(5)),
                    "PARTIAL".equals(paymentStatus) ? "Acompte" : "Règlement total");
        }
    }

    static void insertPayment(String orderId, BigDecimal amount, LocalDateTime date, String note) throws Exception {
        JsonObject p = new JsonObject();
        p.addProperty("order_id", orderId);
        p.addProperty("amount", amount);
        p.addProperty("payment_date", iso(date.isAfter(NOW) ? NOW.minusDays(1) : date));
        p.addProperty("method", RND.nextBoolean() ? "VIREMENT" : "CHEQUE");
        p.addProperty("reference", (RND.nextBoolean() ? "VIR-" : "CHQ-") + "2026-" + String.format("%05d", RND.nextInt(100000)));
        p.addProperty("notes", note);
        insert("payments", p);
    }

    static void buildDelivery(String orderId, String status, LocalDateTime orderDate) throws Exception {
        JsonObject d = new JsonObject();
        d.addProperty("order_id", orderId);
        d.addProperty("status", status);
        d.addProperty("driver_name", SeedCatalog.DRIVERS[RND.nextInt(SeedCatalog.DRIVERS.length)]);
        if ("IN_TRANSIT".equals(status) || "DELIVERED".equals(status)) {
            d.addProperty("latitude", jitter(RABAT_LAT));
            d.addProperty("longitude", jitter(RABAT_LON));
        }
        d.addProperty("notes", switch (status) {
            case "DELIVERED" -> "Colis livré et signé à la réception.";
            case "IN_TRANSIT" -> "Colis en cours d'acheminement vers l'établissement.";
            default -> "Commande en préparation à l'entrepôt.";
        });
        d.addProperty("updated_at", iso(orderDate.plusDays("DELIVERED".equals(status) ? 5 : 2)));
        insert("deliveries", d);
    }

    static void seedStandaloneQuote(String clientId, String status, LocalDateTime createdAt) throws Exception {
        List<Line> lines = pickLines(false);
        BigDecimal total = BigDecimal.ZERO;
        for (Line l : lines) total = total.add(l.lineTotal());
        total = scale(total);
        JsonObject q = new JsonObject();
        q.addProperty("client_id", clientId);
        q.addProperty("status", status);
        q.addProperty("total_amount", total);
        q.addProperty("notes", switch (status) {
            case "PENDING" -> "Nouvelle demande de devis en attente de traitement.";
            case "PROCESSING" -> "Devis en cours de préparation par le service commercial.";
            case "VALIDATED" -> "Devis validé, en attente de commande.";
            default -> "Devis rejeté — budget non disponible.";
        });
        q.addProperty("quote_number", "DEV-2026-" + (quoteSeq++));
        q.addProperty("created_at", iso(createdAt));
        q.addProperty("updated_at", iso(createdAt.plusDays(1)));
        String quoteId = insert("quotes", q);
        for (Line l : lines) addItem("quote_items", "quote_id", quoteId, l);
    }

    static void seedMaintenance(List<OrderRec> orders) throws Exception {
        List<Prod> equip = new ArrayList<>();
        for (OrderRec o : orders) {
            if (!"DELIVERED".equals(o.status())) continue;
            for (Line l : o.lines()) {
                if (l.prod().isEquipment() && equip.stream().noneMatch(e -> e.id().equals(l.prod().id()))) {
                    equip.add(l.prod());
                }
            }
        }
        int max = Math.min(4, equip.size());
        int contracts = 0, interventions = 0;
        for (int i = 0; i < max; i++) {
            Prod prod = equip.get(i);
            int months = prod.warrantyMonths() > 0 ? prod.warrantyMonths() : 24;
            // Make the first contract clearly EXPIRED (started before its warranty length), the rest ACTIVE.
            LocalDate startDate = (i == 0)
                    ? NOW.toLocalDate().minusMonths(months + 4L)
                    : NOW.minusDays(330 - i * 40).toLocalDate();
            LocalDate endDate = startDate.plusMonths(months);
            boolean expired = endDate.isBefore(NOW.toLocalDate());
            String status = expired ? "EXPIRED" : "ACTIVE";

            JsonObject c = new JsonObject();
            c.addProperty("product_id", prod.id());
            c.addProperty("client_id", anzinebClientId);
            c.addProperty("start_date", startDate.toString());
            c.addProperty("end_date", endDate.toString());
            c.addProperty("status", status);
            c.addProperty("notes", "Contrat de maintenance " + months + " mois — Hôpital Ibn Sina.");
            c.addProperty("created_at", iso(startDate.atStartOfDay()));
            String contractId = insert("maintenance_contracts", c);
            contracts++;

            int nInt = 1 + RND.nextInt(2);
            for (int k = 0; k < nInt; k++) {
                LocalDate intvDate = startDate.plusDays(60L + RND.nextInt(200));
                if (intvDate.isAfter(NOW.toLocalDate())) intvDate = NOW.toLocalDate().minusDays(5);
                JsonObject in = new JsonObject();
                in.addProperty("contract_id", contractId);
                in.addProperty("intervention_date", intvDate.toString());
                in.addProperty("description", SeedCatalog.MAINTENANCE_DESCRIPTIONS[RND.nextInt(SeedCatalog.MAINTENANCE_DESCRIPTIONS.length)]);
                in.addProperty("technician_name", SeedCatalog.TECHNICIANS[RND.nextInt(SeedCatalog.TECHNICIANS.length)]);
                in.addProperty("result", SeedCatalog.MAINTENANCE_RESULTS[RND.nextInt(SeedCatalog.MAINTENANCE_RESULTS.length)]);
                in.addProperty("created_at", iso(intvDate.atStartOfDay()));
                insert("maintenance_interventions", in);
                interventions++;
            }
        }
        System.out.println("[seed]   anzineb3: " + contracts + " maintenance contracts, " + interventions + " interventions");
    }

    static void seedTenders() throws Exception {
        // {title, status, createdDaysAgo, deadlineDaysFromNow}
        Object[][] defs = {
            {"Équipement service réanimation 2026", "OPEN",   30, 25},
            {"Consommables stériles — marché annuel", "OPEN",   18, 20},
            {"Renouvellement parc de monitorage",     "CLOSED", 95, -15},
            {"Mobilier médical nouveau bâtiment",      "CLOSED", 130, -40},
        };
        for (Object[] def : defs) {
            String title = (String) def[0];
            String status = (String) def[1];
            int createdDaysAgo = (Integer) def[2];
            int deadlineFromNow = (Integer) def[3];
            List<Line> lines = pickLines(true);
            BigDecimal estimate = BigDecimal.ZERO;
            for (Line l : lines) estimate = estimate.add(l.lineTotal());
            JsonObject t = new JsonObject();
            t.addProperty("client_id", anzinebClientId);
            t.addProperty("title", title);
            t.addProperty("description", "Appel d'offres de l'Hôpital Ibn Sina : " + title
                    + ". Soumission ouverte aux fournisseurs agréés.");
            t.addProperty("budget_max", scale(estimate.multiply(new BigDecimal("1.10"))));
            t.addProperty("deadline", iso(NOW.plusDays(deadlineFromNow)));
            t.addProperty("status", status);
            t.addProperty("created_at", iso(NOW.minusDays(createdDaysAgo)));
            insert("tenders", t);
        }
        System.out.println("[seed]   anzineb3: " + defs.length + " tenders");
    }

    static void seedAnzinebNotifications(List<OrderRec> orders) throws Exception {
        List<JsonObject> notifs = new ArrayList<>();
        for (OrderRec o : orders) {
            notifs.add(notif("Nouvelle commande enregistrée",
                    "Votre commande de " + mad(o.total()) + " a été créée.", o.orderDate().plusDays(1)));
            if ("PAID".equals(o.paymentStatus())) {
                notifs.add(notif("Paiement confirmé",
                        "Le règlement de votre commande a été enregistré.", o.orderDate().plusDays(3)));
            } else if ("PARTIAL".equals(o.paymentStatus())) {
                notifs.add(notif("Acompte reçu",
                        "Un acompte a été enregistré pour votre commande.", o.orderDate().plusDays(3)));
            }
            if ("SHIPPED".equals(o.status())) {
                notifs.add(notif("Livraison en cours",
                        "Votre commande est en transit. Suivez-la en temps réel.", o.orderDate().plusDays(2)));
            }
            if ("DELIVERED".equals(o.status())) {
                notifs.add(notif("Commande livrée",
                        "Votre commande a été livrée et réceptionnée.", o.orderDate().plusDays(6)));
            }
        }
        notifs.add(notif("Devis validé", "Un de vos devis est prêt à être converti en commande.", NOW.minusDays(31)));
        notifs.add(notif("Échéance de paiement proche", "Une facture arrive à échéance dans 7 jours.", NOW.minusDays(4)));
        notifs.add(notif("Facture en retard", "Une facture a dépassé sa date d'échéance.", NOW.minusDays(2)));
        notifs.add(notif("Intervention planifiée", "Un technicien interviendra sur un équipement sous contrat.", NOW.minusDays(10)));
        notifs.add(notif("Contrat de maintenance expirant", "Un contrat de maintenance expire bientôt.", NOW.minusDays(6)));
        notifs.add(notif("Appel d'offres ouvert", "Votre appel d'offres est ouvert aux fournisseurs.", NOW.minusDays(8)));
        notifs.add(notif("Bienvenue sur MedSupply Cloud", "Votre compte hôpital est actif. Bonne utilisation !", NOW.minusDays(240)));

        notifs.sort((a, b) -> a.get("created_at").getAsString().compareTo(b.get("created_at").getAsString()));
        int n = notifs.size();
        for (int i = 0; i < n; i++) {
            JsonObject body = notifs.get(i);
            body.addProperty("user_id", anzinebUserId);
            body.addProperty("is_read", i < n - 6); // newest 6 unread
            insert("notifications", body);
        }
        System.out.println("[seed]   anzineb3: " + n + " notifications");
    }

    static void seedOtherClient(String[] c) throws Exception {
        boolean credit = c[1].equals("HOSPITAL") || c[1].equals("LABORATORY");
        int creditDays = credit ? 60 : 0;
        String uid = createUser(c[2], CLIENT_PW, "CLIENT", NOW.minusDays(150));
        String clientId = createClient(uid, c[0], c[1], c[7], c[3], c[4], c[5], c[6], credit, creditDays, NOW.minusDays(150));

        String[][] scen = {
            {"DELIVERED", "PAID",    "DELIVERED",  "70"},
            {"SHIPPED",   "PARTIAL", "IN_TRANSIT", "25"},
            {"PENDING",   "UNPAID",  null,         "10"},
        };
        int nOrders = 2 + RND.nextInt(2);
        for (int i = 0; i < nOrders; i++) {
            String[] sc = scen[i % scen.length];
            LocalDateTime orderDate = NOW.minusDays(Long.parseLong(sc[3]) + i).withHour(11).withSecond(0);
            buildOrderChain(clientId, creditDays, orderDate, sc[0], sc[1], sc[2], false);
        }
        for (int i = 0; i < 2; i++) {
            JsonObject body = notif(i == 0 ? "Commande mise à jour" : "Bienvenue",
                    i == 0 ? "Le statut de votre commande a changé." : "Votre compte est actif.",
                    NOW.minusDays(20L - i * 5));
            body.addProperty("user_id", uid);
            body.addProperty("is_read", i == 1);
            insert("notifications", body);
        }
    }

    // =====================================================================================
    //  WIPE (surgical: only the seeded demo accounts and their children)
    // =====================================================================================
    static void wipe() throws Exception {
        List<String> userIds = new ArrayList<>();
        for (String e : allDemoEmails()) {
            userIds.addAll(ids(getRows("users", "email=eq." + SupabaseClient.enc(e) + "&select=id"), "id"));
        }
        if (userIds.isEmpty()) {
            System.out.println("[seed]   nothing to wipe (no demo users found).");
            return;
        }
        List<String> clientIds = idsWhereIn("clients", "user_id", userIds, "id");
        List<String> supplierIds = idsWhereIn("suppliers", "user_id", userIds, "id");
        List<String> orderIds = idsWhereIn("orders", "client_id", clientIds, "id");
        List<String> quoteIds = idsWhereIn("quotes", "client_id", clientIds, "id");
        List<String> contractIds = idsWhereIn("maintenance_contracts", "client_id", clientIds, "id");

        deleteIn("notifications", "user_id", userIds);
        deleteIn("maintenance_interventions", "contract_id", contractIds);
        deleteIn("maintenance_contracts", "id", contractIds);
        deleteIn("tenders", "client_id", clientIds);
        deleteIn("payments", "order_id", orderIds);
        deleteIn("deliveries", "order_id", orderIds);
        deleteIn("order_items", "order_id", orderIds);
        deleteIn("orders", "id", orderIds);
        deleteIn("quote_items", "quote_id", quoteIds);
        deleteIn("quotes", "id", quoteIds);
        deleteIn("products", "supplier_id", supplierIds);
        deleteIn("clients", "id", clientIds);
        deleteIn("suppliers", "id", supplierIds);
        deleteIn("users", "id", userIds);
        System.out.println("[seed]   wiped demo data for " + userIds.size() + " demo user(s).");
    }

    static List<String> allDemoEmails() {
        List<String> emails = new ArrayList<>();
        emails.add(ADMIN_EMAIL);
        emails.add(ANZINEB_EMAIL);
        for (String[] s : SeedCatalog.SUPPLIERS) emails.add(s[2]);
        for (String[] c : SeedCatalog.OTHER_CLIENTS) emails.add(c[2]);
        return emails;
    }

    // =====================================================================================
    //  Users / clients (idempotent: reuse if already present)
    // =====================================================================================
    static String createUser(String email, String password, String role, LocalDateTime createdAt) throws Exception {
        JsonArray ex = getRows("users", "email=eq." + SupabaseClient.enc(email) + "&select=id");
        if (ex != null && ex.size() > 0) return ex.get(0).getAsJsonObject().get("id").getAsString();
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password_hash", BCrypt.hashpw(password, BCrypt.gensalt()));
        body.addProperty("role", role);
        body.addProperty("is_active", true);
        body.addProperty("email_verified", true);
        body.addProperty("created_at", iso(createdAt));
        body.addProperty("updated_at", iso(createdAt));
        return insert("users", body);
    }

    static String createClient(String userId, String orgName, String type, String contactName, String phone,
                               String address, String city, String ice, boolean creditEligible, int creditDays,
                               LocalDateTime createdAt) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        body.addProperty("type", type);
        body.addProperty("organization_name", orgName);
        body.addProperty("contact_name", contactName);
        body.addProperty("phone", phone);
        body.addProperty("address", address);
        body.addProperty("city", city);
        body.addProperty("country", "Maroc");
        body.addProperty("ice", ice);
        body.addProperty("credit_limit_days", creditDays);
        body.addProperty("credit_days", creditDays);
        body.addProperty("credit_eligible", creditEligible);
        body.addProperty("is_verified", true);
        body.addProperty("created_at", iso(createdAt));
        body.addProperty("updated_at", iso(createdAt));
        return insert("clients", body);
    }

    // =====================================================================================
    //  Low-level Supabase helpers
    // =====================================================================================
    /** POST a row, bump the per-table count, return the new row's id. */
    static String insert(String table, JsonObject body) throws Exception {
        String resp = SupabaseClient.post(table, body.toString());
        JsonArray arr = SupabaseClient.parseJsonArray(resp);
        if (arr == null || arr.size() == 0) throw new RuntimeException("Insert into " + table + " returned no row: " + resp);
        COUNTS.merge(table, 1, Integer::sum);
        JsonObject row = arr.get(0).getAsJsonObject();
        return row.has("id") && !row.get("id").isJsonNull() ? row.get("id").getAsString() : null;
    }

    static JsonArray getRows(String table, String filters) throws Exception {
        return SupabaseClient.parseJsonArray(SupabaseClient.get(table, filters));
    }

    static List<String> ids(JsonArray rows, String key) {
        List<String> out = new ArrayList<>();
        if (rows == null) return out;
        for (JsonElement el : rows) {
            JsonObject o = el.getAsJsonObject();
            if (o.has(key) && !o.get(key).isJsonNull()) out.add(o.get(key).getAsString());
        }
        return out;
    }

    static List<String> idsWhereIn(String table, String filterCol, List<String> parents, String selectCol) throws Exception {
        if (parents.isEmpty()) return new ArrayList<>();
        return ids(getRows(table, filterCol + "=in.(" + String.join(",", parents) + ")&select=" + selectCol), selectCol);
    }

    static void deleteIn(String table, String col, List<String> values) throws Exception {
        if (values == null || values.isEmpty()) return;
        SupabaseClient.deleteWithFilters(table, col + "=in.(" + String.join(",", values) + ")");
    }

    // ---- generators / formatting ----------------------------------------------
    static List<Line> pickLines(boolean ensureEquipment) {
        int n = 2 + RND.nextInt(3);
        List<Prod> shuffled = new ArrayList<>(pool);
        java.util.Collections.shuffle(shuffled, RND);
        List<Line> lines = new ArrayList<>();
        boolean hasEquip = false;
        for (int i = 0; i < n && i < shuffled.size(); i++) {
            Prod p = shuffled.get(i);
            if (p.isEquipment()) hasEquip = true;
            lines.add(new Line(p, qtyFor(p)));
        }
        if (ensureEquipment && !hasEquip) {
            for (Prod p : shuffled) {
                if (p.isEquipment()) { lines.set(0, new Line(p, qtyFor(p))); break; }
            }
        }
        return lines;
    }

    static int qtyFor(Prod p) {
        if (p.unitPrice().compareTo(new BigDecimal("3000")) < 0) return 10 + RND.nextInt(10) * 5; // 10-55
        if (p.unitPrice().compareTo(new BigDecimal("60000")) < 0) return 2 + RND.nextInt(6);       // 2-7
        return 1 + RND.nextInt(2);                                                                  // 1-2
    }

    static JsonObject notif(String title, String message, LocalDateTime createdAt) {
        JsonObject b = new JsonObject();
        b.addProperty("title", title);
        b.addProperty("message", message);
        b.addProperty("created_at", iso(createdAt));
        return b;
    }

    static String iso(LocalDateTime dt) { return dt.truncatedTo(ChronoUnit.SECONDS).toString(); }
    static BigDecimal scale(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
    static double jitter(double base) { return Math.round((base + (RND.nextDouble() - 0.5) * 0.06) * 1_000_000d) / 1_000_000d; }
    static String mad(BigDecimal v) { return v.setScale(0, RoundingMode.HALF_UP) + " MAD"; }

    // =====================================================================================
    //  Summary / verification / logins
    // =====================================================================================
    static void printSummary() {
        System.out.println("\n---- Inserted rows ----");
        int total = 0;
        for (Map.Entry<String, Integer> e : COUNTS.entrySet()) {
            System.out.printf("  %-28s %5d%n", e.getKey(), e.getValue());
            total += e.getValue();
        }
        System.out.println("  " + "-".repeat(34));
        System.out.printf("  %-28s %5d%n", "TOTAL", total);
    }

    static void verify() throws Exception {
        System.out.println("\n---- Verification (re-read from Supabase) ----");
        int orders = getRows("orders", "client_id=eq." + SupabaseClient.enc(anzinebClientId) + "&select=id").size();
        int quotes = getRows("quotes", "client_id=eq." + SupabaseClient.enc(anzinebClientId) + "&select=id").size();
        int tenders = getRows("tenders", "client_id=eq." + SupabaseClient.enc(anzinebClientId) + "&select=id").size();
        int notifs = getRows("notifications", "user_id=eq." + SupabaseClient.enc(anzinebUserId) + "&select=id").size();
        System.out.println("  anzineb3 orders:        " + orders);
        System.out.println("  anzineb3 quotes:        " + quotes);
        System.out.println("  anzineb3 tenders:       " + tenders);
        System.out.println("  anzineb3 notifications: " + notifs);
        if (orders == 0) System.out.println("  WARNING: no orders read back — check insert permissions / RLS.");
    }

    static void printLogins() {
        System.out.println("\n---- Demo logins ----");
        System.out.println("  ADMIN     : " + ADMIN_EMAIL + "  /  " + ADMIN_PW);
        System.out.println("  CLIENT *  : " + ANZINEB_EMAIL + "  /  " + ANZINEB_PW + "   (HOSPITAL, the 80% account)");
        System.out.println("  SUPPLIERS : seed_supplier1..3@medsupply.dz  /  " + SUPPLIER_PW);
        System.out.println("  CLIENTS   : seed_clientb..e@medsupply.dz  /  " + CLIENT_PW);
    }
}
