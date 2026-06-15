package com.medsupply.seed;

/**
 * SeedCatalog — static reference data used by {@link SeedData}.
 *
 * <p>Targets the REAL Supabase schema (discovered at build time): Moroccan context, MAD prices.
 * The demo reuses the existing categories / brands / products already in the database; the rows
 * here only define the demo personas (suppliers, other clients) and a small set of extra products
 * owned by the demo suppliers so their dashboards aren't empty. Pure data — no DB logic.
 */
public final class SeedCatalog {

    private SeedCatalog() {}

    // Demo suppliers (each -> a SUPPLIER user + supplier profile).
    // {companyName, contactName, email, phone, address, city}
    public static final String[][] SUPPLIERS = {
        {"MediTech Maroc",        "Karim El Fassi",  "seed_supplier1@medsupply.dz", "0522 55 41 10", "Zone Industrielle Ain Sebaa", "Casablanca"},
        {"SantéPlus Distribution","Nadia Berrada",   "seed_supplier2@medsupply.dz", "0537 33 28 77", "Avenue Mohammed VI",          "Rabat"},
        {"BioMed Solutions",      "Yassine Tazi",    "seed_supplier3@medsupply.dz", "0524 92 14 60", "Quartier Gueliz",             "Marrakech"},
    };

    // Other demo clients (the ~20%). {orgName, type, email, phone, address, city, ice, contactName}
    public static final String[][] OTHER_CLIENTS = {
        {"Clinique El Andalous",       "CLINIC",         "seed_clientb@medsupply.dz", "0522 60 22 18", "12 Rue Ibn Sina",        "Casablanca", "ICE002201450", "Dr. Samir Lahlou"},
        {"Cabinet Dr. Benjelloun",     "DOCTOR",         "seed_clientc@medsupply.dz", "0524 78 09 33", "5 Boulevard Zerktouni",  "Marrakech",  "ICE002203910", "Dr. Réda Benjelloun"},
        {"Laboratoire BioRabat",       "LABORATORY",     "seed_clientd@medsupply.dz", "0537 64 50 27", "Avenue de France",       "Rabat",      "ICE002210070", "Mme Lila Cherkaoui"},
        {"MediStore Distribution",     "RESELLER",       "seed_cliente@medsupply.dz", "0539 21 88 44", "Zone Franche",           "Tanger",     "ICE002215600", "M. Tarek Ouazzani"},
    };

    /**
     * Extra products owned by the demo suppliers. catIdx/brandIdx index into the EXISTING
     * categories/brands fetched at runtime (taken modulo the available count); supIdx -> SUPPLIERS.
     * {name, reference, description, unitPriceMAD, warrantyMonths, catIdx, brandIdx, supIdx}
     */
    public static final String[][] NEW_PRODUCTS = {
        {"Échographe portable SD",      "ECHO-SD20", "Échographe portable multi-sondes, écran tactile 15 pouces", "480000", "24", "0", "0", "0"},
        {"Moniteur de chevet compact",  "MON-BD12",  "Moniteur de chevet ECG/SpO2/NIBP écran 12 pouces",          "142000", "24", "1", "3", "0"},
        {"Pousse-seringue électrique",  "PSE-DZ50",  "Pousse-seringue de précision programmable",                 "38000",  "24", "1", "5", "0"},
        {"Défibrillateur biphasique",   "DEF-LX9",   "Défibrillateur biphasique avec moniteur intégré",          "95000",  "36", "6", "5", "0"},
        {"Lit médicalisé électrique",   "BED-EL3",   "Lit médicalisé électrique 3 fonctions avec barrières",      "175000", "36", "2", "6", "1"},
        {"Chariot de soins inox",       "CART-IX4",  "Chariot de soins inox 4 tiroirs avec poubelle",             "28000",  "24", "2", "2", "1"},
        {"Table d'examen 2 sections",   "TBL-EX2",   "Table d'examen 2 sections avec dérouleur papier",           "32000",  "24", "2", "1", "1"},
        {"Centrifugeuse 24 tubes",      "CENT-L24",  "Centrifugeuse de laboratoire 24 tubes vitesse réglable",    "88000",  "24", "4", "2", "1"},
        {"Microscope binoculaire",      "MIC-B40",   "Microscope binoculaire 1000x à éclairage LED",              "56000",  "24", "4", "1", "2"},
        {"Bistouri électrique 400W",    "BST-E400",  "Générateur de bistouri électrique 400W mono/bipolaire",     "132000", "24", "3", "7", "2"},
        {"Aspirateur chirurgical 30L",  "ASP-S30",   "Aspirateur chirurgical mobile 30L haute dépression",        "44000",  "24", "3", "3", "2"},
        {"Réfrigérateur médical 200L",  "REF-M200",  "Réfrigérateur médical 200L à température contrôlée",        "63000",  "36", "4", "7", "2"},
    };

    public static final String[] DRIVERS = {
        "Mohamed Alami", "Hassan Idrissi", "Rachid Bennani", "Younes Sefiani", "Omar Chraibi"
    };

    public static final String[] TECHNICIANS = {
        "Technicien Amine Fassi", "Technicien Karim Saidi", "Technicien Walid Naciri",
        "Technicien Hamza Bekkali", "Technicien Othmane Riad"
    };

    public static final String[] MAINTENANCE_DESCRIPTIONS = {
        "Maintenance préventive annuelle — calibration des capteurs",
        "Remplacement de la batterie interne",
        "Vérification et nettoyage du circuit pneumatique",
        "Mise à jour du firmware et test fonctionnel complet",
        "Réparation de l'écran d'affichage",
        "Contrôle de sécurité électrique et calibration",
    };

    public static final String[] MAINTENANCE_RESULTS = {
        "Conforme — aucun défaut détecté",
        "Pièce remplacée, appareil testé conforme",
        "Calibration effectuée, retour en service",
        "Intervention en cours, pièce en commande",
    };

    // Paired arrays: MAINTENANCE_PROBLEMS[i] (client-reported issue, used for the request
    // description) corresponds to MAINTENANCE_DIAGNOSES[i] (technician finding). Same length.
    public static final String[] MAINTENANCE_PROBLEMS = {
        "L'appareil ne s'allume plus",
        "Écran figé pendant l'utilisation",
        "Mesures incohérentes / hors tolérance",
        "Alarme intempestive récurrente",
        "Surchauffe après quelques minutes de fonctionnement",
        "Bruit anormal et vibrations en marche",
    };

    public static final String[] MAINTENANCE_DIAGNOSES = {
        "Alimentation défectueuse — bloc à remplacer",
        "Firmware corrompu — réinitialisation de la carte mère",
        "Capteur déréglé — recalibration nécessaire",
        "Faux contact sur le circuit d'alarme",
        "Ventilateur de refroidissement hors service",
        "Roulement usé — remplacement requis",
    };
}
