# MedSupply Cloud — Questions / Réponses (Analyse technique du projet)

> Document d'analyse en français du projet **MedSupply Cloud**.
> Format : questions / réponses, regroupées par thème.
> Objectif : comprendre l'ensemble du projet — sa structure, ses outils, son fonctionnement technique.

---

## Table des matières

1. [Vue d'ensemble](#1-vue-densemble)
2. [Architecture générale](#2-architecture-générale)
3. [Le backend Java en détail](#3-le-backend-java-en-détail)
4. [La base de données (Supabase / PostgREST)](#4-la-base-de-données-supabase--postgrest)
5. [Authentification & sécurité](#5-authentification--sécurité)
6. [L'API REST & les endpoints](#6-lapi-rest--les-endpoints)
7. [Le frontend](#7-le-frontend)
8. [Le temps réel (Realtime)](#8-le-temps-réel-realtime)
9. [Build, outils & déploiement](#9-build-outils--déploiement)
10. [Configuration & secrets](#10-configuration--secrets)
11. [Documentation de l'API (Swagger / OpenAPI)](#11-documentation-de-lapi-swagger--openapi)
12. [Conventions, pièges & points d'attention](#12-conventions-pièges--points-dattention)

---

## 1. Vue d'ensemble

**Q : Qu'est-ce que MedSupply Cloud ?**
R : C'est une **plateforme B2B de fourniture de matériel médical**. Elle met en relation des **clients** (établissements de santé), des **fournisseurs** et des **administrateurs**. Elle couvre tout le cycle : catalogue produits, demandes de devis, commandes, livraisons, paiements, appels d'offres (tenders), contrats de maintenance et notifications.

**Q : Quelle est la pile technologique (stack) ?**
R :
- **Backend** : Java 21, Jakarta Servlet (Jakarta EE 6) tournant sur **Tomcat 10+**, exposant une **API REST JSON**.
- **Base de données** : **Supabase**, accédée **uniquement via son API HTTP PostgREST** (pas de JDBC, pas de couche SQL locale).
- **Frontend** : HTML statique + **Tailwind CSS** (via CDN) + **JavaScript vanilla (ES6)**, sans étape de build.
- **JSON** : bibliothèque **Gson** (Google).
- **Mots de passe** : **BCrypt** (jBCrypt).
- **Email** : **Jakarta Mail** (JavaMail).

**Q : La langue du projet ?**
R : Le code est en anglais (noms de classes, méthodes), mais une grande partie des **textes de l'interface et des commentaires sont en français**.

**Q : Quelle est l'ampleur du projet (chiffres clés) ?**
R :
- ~**70 fichiers Java** organisés en couches (controllers, services, dao, models, utils, filters, seed).
- **17 servlets** (contrôleurs) exposant l'API.
- **~79 endpoints** documentés dans l'OpenAPI.
- **47 pages HTML** côté frontend.

---

## 2. Architecture générale

**Q : Quelle est l'architecture globale ?**
R : Une **architecture en couches stricte à 4 niveaux** :

```
Servlet (controllers/*)  →  Service (services/*)  →  DAO (dao/*)  →  SupabaseClient  →  Supabase PostgREST
```

Chaque couche a une responsabilité unique et ne communique qu'avec la couche immédiatement en dessous.

**Q : Quel est le rôle de chaque couche ?**
R :
- **Controllers (Servlets)** : reçoivent la requête HTTP, lisent la session (`role`/`userId`), font le **contrôle d'autorisation en ligne (inline)**, appellent un service, et écrivent la réponse JSON. Ils ne touchent **jamais** la base directement.
- **Services** : contiennent la **logique métier** et la validation (ex. machines à états de statut, calcul des totaux, règles de conversion devis→commande).
- **DAOs** : traduisent vers/depuis Supabase. Ils construisent **à la main** les chaînes de filtres PostgREST et mappent le JSON vers les modèles.
- **SupabaseClient** : un **wrapper HTTP statique** (get/post/patch/delete) au-dessus de `java.net.http.HttpClient`.

**Q : Comment est organisé le code par domaine métier ?**
R : Chaque domaine fonctionnel possède son **quadruplet** : un *controller* + un *service* + un *DAO* + un (ou plusieurs) *model(s)*. Les domaines : orders (commandes), quotes (devis), tenders (appels d'offres), deliveries (livraisons), payments (paiements), products (produits), maintenance, notifications, clients, suppliers, etc.

**Q : Quelle est la structure des packages Java ?**
R :
```
com.medsupply
├── controllers/   → les @WebServlet (point d'entrée HTTP)
├── services/      → logique métier
├── dao/           → accès aux données via Supabase
├── models/        → POJOs (un par table)
├── filters/       → AuthFilter, CorsFilter
├── utils/         → SupabaseClient, EmailService, JsonUtil, SupabaseException
└── seed/          → SeedData, SeedCatalog (jeu de données de démo)
```

---

## 3. Le backend Java en détail

**Q : Comment est défini un endpoint (un contrôleur) ?**
R : Avec l'annotation `@WebServlet("/api/...")`. Les patterns utilisent souvent un joker, ex. `@WebServlet("/api/orders/*")`, ce qui permet à un même servlet de gérer plusieurs sous-routes (`/api/orders`, `/api/orders/{id}`, `/api/orders/{id}/status`, …) en analysant le `pathInfo`.

**Q : Quelle forme ont les réponses de l'API ?**
R : Toujours une enveloppe JSON Gson de la forme :
```json
{ "success": true|false, "message": "...", ... }
```
Les données supplémentaires sont ajoutées au même objet.

**Q : Où se trouve la logique métier ? Un exemple ?**
R : Dans les **services**. Par exemple `OrderService` :
- `convertQuoteToOrder(quoteId)` : vérifie le statut du devis, empêche une **double conversion**, et calcule les totaux.
- `updateOrderStatus(...)` avec `isValidStatusTransition(...)` : implémente la **machine à états** des commandes :
  `PENDING → PROCESSING → SHIPPED → DELIVERED → COMPLETED`.

**Q : Comment fonctionne le SupabaseClient ?**
R : C'est une classe **statique** (toutes les méthodes sont `static`) qui encapsule un `HttpClient`. Elle expose :
- `get(table, filters)`, `post(table, body)`, `patch(table, id, body)`, `patchWithFilters(...)`, `delete(table, id)`, `deleteWithFilters(table, filters)`.
- Des helpers JSON : `toJson`, `fromJson`, `parseJsonArray`, `parseJsonObject`.
- `enc(value)` : **URL-encode** une valeur dynamique (protection contre l'injection de filtre PostgREST).
- `affectedRows(body)` : compte les lignes affectées par un PATCH/DELETE (car PostgREST renvoie `"[]"` — une chaîne non vide — quand zéro ligne ne correspond).
- L'en-tête `Prefer: return=representation` est posé ici pour récupérer les lignes créées/modifiées.

**Q : Comment les erreurs sont-elles gérées ?**
R : Les échecs DAO remontent sous forme de `SupabaseException`. Son `getMessage()` est **générique** (le corps brut renvoyé par PostgREST n'est journalisé que côté serveur), afin de ne pas exposer les détails du schéma. Les contrôleurs peuvent donc renvoyer ce message sans fuite d'information.

**Q : Que sont les modèles (`models/`) ?**
R : De simples **POJOs**, un par table. Particularité : les **noms de colonnes en base sont en snake_case** (`password_hash`, `quote_id`, `total_amount`), tandis que les **getters Java sont en camelCase**. Le mapping se fait explicitement dans les DAOs avec des gardes du type `json.has(...) && !json.get(...).isJsonNull()`.

---

## 4. La base de données (Supabase / PostgREST)

**Q : Y a-t-il une base SQL locale / du JDBC ?**
R : **Non.** Il n'y a **aucune couche SQL/JDBC locale**. La « base de données » est **Supabase**, et on y accède **exclusivement via son API HTTP PostgREST**. (Attention : un ancien document décrit le backend comme « JDBC » — c'est **inexact**.)

**Q : Comment les DAOs interrogent-ils la base ?**
R : Ils construisent **manuellement** des chaînes de filtres PostgREST, par exemple :
- `"id=eq." + id`
- `"client_id=eq." + id + "&order=created_at.desc"`

Chaque **valeur dynamique** doit être enveloppée dans `SupabaseClient.enc(...)`.

**Q : Comment gérer les enregistrements sans colonne `client_id` (paiements, livraisons) ?**
R : On résout la **propriété** (ownership) via la commande parente dans la couche service. Voir `PaymentService` / `DeliveryService` et leur méthode `enrichClientId`.

**Q : Y a-t-il un script de schéma SQL dans le dépôt ?**
R : Oui, un fichier de migration `database/migration_align_schema.sql` (alignement du schéma). Le schéma lui-même vit dans Supabase.

**Q : Existe-t-il des données de démonstration ?**
R : Oui. Le package `seed/` contient `SeedData` et `SeedCatalog`. On les exécute via le plugin `exec-maven-plugin` :
```bash
mvn -q compile exec:java -Dexec.mainClass=com.medsupply.seed.SeedData
```
(Passer l'argument `append` pour ne pas vider les tables avant insertion.)

---

## 5. Authentification & sécurité

**Q : Quel est le mécanisme d'authentification ?**
R : Une authentification **par session HTTP basée sur cookie** (`HttpSession`), **pas** par token JWT. À la connexion, `LoginServlet` valide via `AuthService`/`UserDAO` (BCrypt `checkpw` contre la colonne `password_hash`), puis stocke `user`, `userId` et `role` dans la session. **Timeout : 30 minutes.**

**Q : Comment les routes sont-elles protégées ?**
R : Par `AuthFilter`, un `@WebFilter("/api/*")` qui bloque toute requête non authentifiée. Si aucun `userId` n'est présent en session, il renvoie un **401 JSON** : `{"success": false, "message": "Unauthorized - Please login"}`.

**Q : Quelles routes sont publiques (non protégées) ?**
R : Quatre chemins :
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/verify`
- `/api/test`

**Q : Quels sont les rôles ?**
R : Trois rôles : **`ADMIN`**, **`CLIENT`**, **`SUPPLIER`**.

**Q : Comment se fait le contrôle d'accès par rôle ?**
R : **À la main, en ligne (inline)** dans chaque servlet : on lit `session.getAttribute("role")` et on compare. Il **n'y a pas** de framework d'annotations de rôle — il faut reproduire ce pattern manuel pour tout nouvel endpoint.

**Q : Comment fonctionne l'inscription (registration) ?**
R : Elle crée un utilisateur **non vérifié**, génère un **token UUID de vérification d'email** (expiration 24h), et l'envoie par mail via `EmailService`. La **connexion est refusée (403)** tant que l'email n'est pas vérifié.

**Q : Les mots de passe sont-ils sécurisés ?**
R : Oui, hachés avec **BCrypt** (jBCrypt) et stockés dans la colonne `password_hash`. La vérification se fait avec `BCrypt.checkpw`.

**Q : Y a-t-il du CORS ?**
R : Oui, `CorsFilter` autorise **uniquement** `http://localhost:5500` et `http://127.0.0.1:5500` avec credentials — c'est une config **de développement**, marquée TODO-pour-la-production (à durcir avant déploiement).

---

## 6. L'API REST & les endpoints

**Q : Quelle est l'URL de base de l'API ?**
R : L'application tourne sous le context path `/medsupply-cloud`, donc :
`http://localhost:8080/medsupply-cloud/api`

**Q : Quels sont les principaux groupes d'endpoints ?**
R : (issus des `@WebServlet`)

| Servlet | Route | Domaine |
|---|---|---|
| `LoginServlet` | `/api/auth/login` | Connexion |
| `RegisterServlet` | `/api/auth/register` | Inscription |
| `VerifyEmailServlet` | `/api/auth/verify` | Vérification email |
| `LogoutServlet` | `/api/auth/logout` | Déconnexion |
| `ProductServlet` | `/api/products/*` | Produits / catalogue |
| `OrderServlet` | `/api/orders/*` | Commandes |
| `QuoteServlet` | `/api/quotes/*` | Devis |
| `TenderServlet` | `/api/tenders/*` | Appels d'offres |
| `DeliveryServlet` | `/api/deliveries/*` | Livraisons |
| `PaymentServlet` | `/api/payments/*` | Paiements |
| `MaintenanceServlet` | `/api/maintenance/*` | Maintenance |
| `NotificationServlet` | `/api/notifications/*` | Notifications |
| `ClientServlet` | `/api/clients/*` | Clients |
| `SupplierServlet` | `/api/suppliers/*` | Fournisseurs |
| `ProfileServlet` | `/api/profile/*` | Profil utilisateur |
| `AdminDashboardServlet` | `/api/admin/dashboard/*` | Tableau de bord admin |
| `TestConnectionServlet` | `/api/test` | Test de connexion (public) |

**Q : Combien d'endpoints au total ?**
R : Environ **79 endpoints**, tous documentés dans l'OpenAPI.

---

## 7. Le frontend

**Q : Comment est construit le frontend ?**
R : Du **HTML pur + Tailwind CSS (via CDN) + JavaScript vanilla ES6**, **sans étape de build** (pas de Webpack/Vite/npm). Il est embarqué dans le WAR sous `src/main/webapp/` et servi depuis `/medsupply-cloud/`.

**Q : Comment le frontend appelle-t-il l'API ?**
R : Via `js/api.js`, qui code en dur `API_BASE = "/medsupply-cloud/api"`. Le déploiement **same-origin via le WAR** est donc le chemin prévu. Les appels utilisent `credentials: "include"` pour transmettre le cookie de session.

**Q : Comment est organisé le dossier `js/` ?**
R :
- `api.js` — couche d'appel à l'API
- `auth.js` — gestion de l'authentification côté client
- `router.js` — routage des pages
- `mock-data.js` — données simulées (mode mock)
- `components/` — composants UI réutilisables : `header`, `navbar`, `sidebar`, `modal`, `toast`, `table`, `badge`, `loader`, `skeleton`, `notification-bell`
- `utils/` — `format.js`, `validate.js`

**Q : Comment sont organisées les pages HTML ?**
R : Dans `src/main/webapp/pages/`, segmentées par rôle :
- `pages/admin/` — dashboard, clients, commandes, devis, livraisons, paiements, produits, stocks, maintenance, marketplace, créances (receivables)…
- `pages/client/` — catalogue, dashboard, commandes, devis, paiements, maintenance, marketplace…
- `pages/supplier/` — dashboard, offres, marketplace…
- `pages/auth/` — login, register, pending (en attente de vérification)
- Plus des pages racine : `cart.html`, `checkout.html`, `products.html`, `product-detail.html`, `profile.html`.

**Q : Y a-t-il plusieurs frontends dans le dépôt ? Attention !**
R : Oui, **deux arbres frontend coexistent** :
- `src/main/webapp/` → c'est **le vrai frontend complet** (47 pages HTML, composants complets). **C'est celui à utiliser.**
- `medsupply-cloud-frontend/` → une copie **partielle/secondaire** (seulement 6 pages client). La doc qui le décrit comme « complet » est **aspirationnelle**, pas le reflet de l'état réel.

**Q : Qu'est-ce que le drapeau `USE_MOCK_API` ?**
R : Un flag dans `js/api.js` (actuellement `false`) qui, s'il est activé, route les appels vers `js/mock-data.js` au lieu du serveur réel. Plusieurs fichiers markdown (`QUICK_START_GUIDE.md`, `ECOMMERCE_README.md`…) documentent ce **mode démo mock** et ne reflètent pas forcément l'app branchée au backend.

---

## 8. Le temps réel (Realtime)

**Q : Le backend ouvre-t-il des WebSockets ?**
R : **Non.** Le backend n'ouvre **aucun WebSocket**.

**Q : Comment fonctionne alors le temps réel ?**
R : Les **clients frontend s'abonnent directement aux canaux Supabase Realtime**. Comme le backend écrit dans la base via PostgREST, toute écriture est **automatiquement diffusée (broadcast)** par Supabase aux abonnés. La liste des tables et des exemples d'abonnement se trouvent dans `SUPABASE_REALTIME_SETUP.md`.

> Remarque : ce document décrit (à tort) le backend comme « JDBC ». En réalité, il utilise l'API HTTP PostgREST.

---

## 9. Build, outils & déploiement

**Q : Comment compiler et empaqueter le projet ?**
R : Avec un **Maven système** (il n'y a pas de Maven wrapper). Java 21 requis (`maven.compiler.source/target=21`).
```bash
mvn clean package   # produit target/medsupply-cloud.war
mvn compile         # compilation seule
```

**Q : Quel format d'artefact ? Où le déployer ?**
R : Un **WAR** : `target/medsupply-cloud.war`, à déployer sur **Tomcat 10+** (Jakarta, **pas** `javax`). Context path : `/medsupply-cloud`.

**Q : Quelles sont les dépendances Maven ?**
R :
- `jakarta.servlet-api` 6.0.0 (scope `provided` — fourni par Tomcat)
- `gson` 2.10.1 (JSON)
- `slf4j-simple` 2.0.12 (logging)
- `jakarta.mail` 2.0.1 (envoi d'emails)
- `jbcrypt` 0.4 (hachage de mots de passe)

**Q : Quels plugins Maven ?**
R :
- `maven-war-plugin` 3.4.0 (avec `failOnMissingWebXml=false`).
- `exec-maven-plugin` 3.2.0, configuré pour lancer le seeder `com.medsupply.seed.SeedData`.

**Q : Y a-t-il des tests ?**
R : **Non.** Le dossier `src/test` n'existe pas et il n'y a **aucune suite de tests** ni config de lint. Il ne faut donc **pas** prétendre que « les tests passent » — il n'y en a aucun à exécuter.

**Q : Comment lancer le frontend en standalone ?**
R : En servant le dossier `src/main/webapp/` sur HTTP (ex. `python -m http.server`). Mais comme `js/api.js` code en dur l'URL de l'API du WAR, le **déploiement same-origin via le WAR** reste le chemin prévu.

---

## 10. Configuration & secrets

**Q : Comment sont fournis l'URL et la clé Supabase ?**
R : **Jamais en dur dans le code.** `SupabaseClient` les résout dans cet ordre de priorité :
1. Variables d'environnement `SUPABASE_URL` / `SUPABASE_ANON_KEY`
2. Propriétés système JVM `supabase.url` / `supabase.anon.key`
3. Un fichier classpath `src/main/resources/supabase.properties` (ignoré par git ; voir `supabase.properties.example`)

Si aucune source n'est trouvée, l'application **lève une erreur au démarrage** (`ExceptionInInitializerError`).

**Q : Comment l'envoi d'email est-il configuré ?**
R : `EmailService` charge la config SMTP depuis `email.properties` sur le classpath. Si le fichier est absent, **l'email est désactivé** et les tokens de vérification sont **affichés sur la sortie standard (stdout)**. `EmailService.isConfigured()` conditionne l'envoi réel.

**Q : Que contient le `.gitignore` ?**
R : Il exclut notamment `src/main/resources/db.properties`, `target/`, et `*.war`. À noter : `db.properties` **n'est référencé par aucun code actuel** — le chemin de données est 100 % REST.

---

## 11. Documentation de l'API (Swagger / OpenAPI)

**Q : Existe-t-il une documentation d'API interactive ?**
R : Oui :
- Spécification **OpenAPI 3.0** : `src/main/webapp/docs/openapi.json` (couvre les ~79 endpoints, les schémas de modèles, la sécurité par cookie de session). Servie à `/medsupply-cloud/docs/openapi.json`.
- **Swagger UI** : `src/main/webapp/docs/index.html` → accessible à `http://localhost:8080/medsupply-cloud/docs/` après déploiement.

**Q : Pourquoi Swagger n'est-il pas bloqué par l'AuthFilter ?**
R : Parce qu'il vit **en dehors de `/api/*`** (sous `/docs/`), donc `AuthFilter` (qui ne filtre que `/api/*`) ne le bloque pas. Le bouton « Try it out » envoie le cookie de session (`credentials: include`).

**Q : Que faire en cas d'ajout/modification d'endpoint ?**
R : **Mettre à jour `openapi.json`** pour rester synchronisé. La spec a été générée à partir des vraies routes des servlets.

---

## 12. Conventions, pièges & points d'attention

**Q : Comment ajouter proprement un nouvel endpoint ?**
R : Créer le **quadruplet** : nouveau `@WebServlet` (controller) + service + DAO + model. Et :
- Garder l'**autorisation en ligne** dans le servlet.
- Retourner l'enveloppe `{success, message, ...}`.
- **Écrire le corps de la réponse exactement une fois** par chemin de requête (ne pas écrire dans une branche/catch *puis* à nouveau en fin de méthode).

**Q : Quelles précautions dans les DAOs ?**
R :
- Construire les requêtes PostgREST comme des **chaînes de filtres**, et **envelopper chaque valeur dynamique dans `enc(...)`** (anti-injection).
- Pour les suppressions non-`id`, utiliser `deleteWithFilters(table, filters)`.
- Juger le succès d'un PATCH/DELETE avec `affectedRows(response) > 0` (un match vide renvoie `"[]"`, qui est **non vide** en tant que `String`).

**Q : Quels sont les principaux pièges (« gotchas ») ?**
R :
1. **Deux frontends** : seul `src/main/webapp/` est le vrai ; `medsupply-cloud-frontend/` est partiel.
2. **Bearer token no-op** : `js/api.js` envoie un header `Bearer` (depuis `sessionStorage`) **ET** `credentials: "include"`. Le backend n'authentifie **que par cookie de session** — le Bearer est donc **sans effet**.
3. **Docs trompeuses** : `SUPABASE_REALTIME_SETUP.md` parle de « JDBC » (faux) ; plusieurs `.md` décrivent un mode démo mock qui ne reflète pas l'app réelle.
4. **Pas de tests** : ne jamais affirmer que les tests passent.
5. **CORS en mode dev** : à durcir avant production.

**Q : En résumé, quel est l'« esprit » du code à respecter ?**
R : Un code **explicite et manuel** : routage par jokers dans les servlets, autorisation inline par rôle, filtres PostgREST construits à la main avec `enc()`, mapping JSON ↔ POJO explicite, enveloppe de réponse uniforme. Pas de magie / pas de framework lourd — on **réplique les patterns existants** plutôt que d'introduire de nouvelles abstractions.

---

*Document généré à partir d'une analyse du code source (structure des packages, `pom.xml`, annotations `@WebServlet`/`@WebFilter`, `SupabaseClient`, `OrderService`, `AuthFilter`, spec OpenAPI) et du fichier `CLAUDE.md` du dépôt.*
