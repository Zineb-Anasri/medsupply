# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

MedSupply Cloud — a B2B medical supply platform. A Java Servlet (Jakarta EE 6 / Tomcat 10+) backend exposing a JSON REST API, with a static HTML/Tailwind/vanilla-JS frontend. The "database" is **Supabase accessed over its PostgREST HTTP API** — there is no local SQL/JDBC layer. UI text and many comments are in French.

## Build & run

There is no Maven wrapper; use a system `mvn` (Java 21 toolchain required, `maven.compiler.source/target=21`).

```bash
mvn clean package        # produces target/medsupply-cloud.war
mvn compile              # compile only
```

Deploy `target/medsupply-cloud.war` to **Tomcat 10+** (Jakarta, not `javax`). The app runs under context path `/medsupply-cloud`, so the API base is `http://localhost:8080/medsupply-cloud/api`.

There is **no test suite** (`src/test` does not exist) and no lint config. Don't claim tests pass — there are none to run.

### Frontend
The primary frontend is bundled in the WAR at `src/main/webapp/` and served from `/medsupply-cloud/`. It is plain HTML + Tailwind (CDN) + vanilla ES6; no build step. To run it standalone against a separate Tomcat, serve the directory over HTTP (e.g. `python -m http.server`) — but note `js/api.js` hardcodes `API_BASE = "/medsupply-cloud/api"`, so same-origin deployment via the WAR is the intended path.

## Architecture

Request flow is a strict 4-layer stack:

```
Servlet (controllers/*)  →  Service (services/*)  →  DAO (dao/*)  →  SupabaseClient  →  Supabase PostgREST
```

- **Controllers** (`@WebServlet("/api/...")`, path patterns like `/api/orders/*`) parse the request, read the session for `role`/`userId`, do **role-based authorization inline**, call a service, and write a Gson `JsonObject` response of the shape `{ "success": bool, "message": str, ... }`. They never touch the DB directly.
- **Services** hold business logic and validation (e.g. `OrderService.convertQuoteToOrder` enforces quote status, prevents double-conversion, computes totals; state machines like `PENDING→PROCESSING→SHIPPED→DELIVERED→COMPLETED` live here).
- **DAOs** translate to/from Supabase. They build **PostgREST filter strings by hand** — e.g. `"id=eq." + id`, `"client_id=eq." + id + "&order=created_at.desc"` — and map JSON with explicit `json.has(...) && !json.get(...).isJsonNull()` guards. Column names are snake_case DB fields (`password_hash`, `quote_id`, `total_amount`); model getters are camelCase.
- **`utils/SupabaseClient`** is a static HTTP wrapper (`get/post/patch/patchWithFilters/delete`) over `java.net.http.HttpClient`, plus Gson helpers. All Supabase auth headers and the `Prefer: return=representation` header live here.

`models/` are plain POJOs (one per table). Each domain area has a matching controller + service + DAO + model set (orders, quotes, tenders, deliveries, payments, products, maintenance, notifications, etc.).

### Auth & sessions
- Authentication is **HttpSession cookie-based**, not token-based. `LoginServlet` validates via `AuthService`/`UserDAO` (BCrypt `checkpw` against the `password_hash` column), then stores `user`, `userId`, and `role` on the session (30-min timeout).
- `AuthFilter` (`@WebFilter("/api/*")`) blocks unauthenticated requests except the public paths `/api/auth/login`, `/api/auth/register`, `/api/auth/verify`, `/api/test`. It returns 401 JSON when no `userId` is on the session.
- **Roles are `ADMIN`, `CLIENT`, `SUPPLIER`.** Per-endpoint role checks are done by hand inside each servlet (read `session.getAttribute("role")` and compare). There is no annotation/role framework — replicate the existing inline pattern when adding endpoints.
- Registration creates an unverified user, generates a UUID email-verification token (24h expiry), and emails it via `EmailService`; login is rejected with 403 until the email is verified.

### Realtime
The backend does **not** open WebSockets. Frontend clients subscribe to Supabase Realtime channels directly; any DB write the backend makes via PostgREST is auto-broadcast. See `SUPABASE_REALTIME_SETUP.md` for the table list and subscription examples. (Note: that doc describes the backend as "JDBC" — that is inaccurate; the backend uses the PostgREST HTTP API as described above.)

## API documentation (Swagger / OpenAPI)

- OpenAPI 3.0 spec: `src/main/webapp/docs/openapi.json` (covers all ~79 endpoints, model schemas, cookie-session security). Served at `/medsupply-cloud/docs/openapi.json`.
- Swagger UI: `src/main/webapp/docs/index.html` → open `http://localhost:8080/medsupply-cloud/docs/` after deploying. It lives outside `/api/*` so `AuthFilter` does not block it; "Try it out" sends the session cookie (`credentials: include`).
- The spec was generated from the actual servlet routes. When you add/change an endpoint, update `openapi.json` to match.

## Configuration & secrets

- **Supabase URL + anon key are loaded from configuration**, not hardcoded. `SupabaseClient` resolves them (in order) from env vars `SUPABASE_URL`/`SUPABASE_ANON_KEY`, JVM system properties `supabase.url`/`supabase.anon.key`, or a classpath `src/main/resources/supabase.properties` (git-ignored; see `supabase.properties.example`). It throws on startup if none are set.
- DAO failures surface as `utils/SupabaseException`, whose `getMessage()` is generic (the raw PostgREST body is logged server-side only) — controllers can echo it without leaking schema detail.
- `EmailService` loads SMTP settings from `email.properties` on the classpath (`src/main/resources/`); if absent, email is disabled and verification tokens are printed to stdout (`EmailService.isConfigured()` gates sending).
- `.gitignore` excludes `src/main/resources/db.properties` (and `target/`, `*.war`). `db.properties` is not referenced by any current code — the data path is REST-only.
- `CorsFilter` allows only `http://localhost:5500` / `http://127.0.0.1:5500` with credentials, for dev. It is marked TODO-for-production; tighten origins before deploying.

## Conventions when extending

- New endpoint = new `@WebServlet` controller + service + DAO + model, following the existing per-domain quadruple. Keep authorization inline in the servlet and return the `{success, message, ...}` JSON envelope. Write the response body exactly once per request path (don't print in a branch/catch and then again at method end).
- In DAOs, build PostgREST queries as filter strings and **wrap every dynamic value in `SupabaseClient.enc(...)`** (URL-encodes the value to prevent PostgREST filter injection). Use `SupabaseClient.deleteWithFilters(table, filters)` for non-`id` deletes, and judge PATCH/DELETE success with `SupabaseClient.affectedRows(response) > 0` (an empty match returns `"[]"`, which is non-empty as a String).
- For records whose table has no `client_id` column (payments, deliveries), resolve ownership via the owning order in the service layer (see `PaymentService`/`DeliveryService` `enrichClientId`).

## Gotchas / things that look stale

- Two frontend trees exist. `src/main/webapp/` is the real, complete one (46 HTML pages, full `js/components`). `medsupply-cloud-frontend/` contains only 6 client HTML pages and is a partial/secondary copy — the file-structure section in `src/main/webapp/README.md` describing a fully-populated `medsupply-cloud-frontend/` is aspirational, not current.
- `js/api.js` attaches a `Bearer` token from `sessionStorage` **and** sends `credentials: "include"`. The backend authenticates by session cookie only; the Bearer header is effectively a no-op. There is also a `USE_MOCK_API` flag (currently `false`) that routes calls to `js/mock-data.js` instead of the server.
- Several markdown files under `src/main/webapp/` (`QUICK_START_GUIDE.md`, `ECOMMERCE_README.md`, etc.) document a mock-data demo mode and may not reflect the live backend-connected app.
