# Bida Management (Spring Boot)

Spring Boot 3.2 application for running a billiard hall: table booking, billing, staff management, price rules, holiday calendar, products (F&B), and PDF invoice export. The backend lives in `code/backend` and renders server-side pages with Thymeleaf.

## Project layout
- `code/backend` — Spring Boot backend (Java 17, Maven). Main app class: `com.bida.BidaApplication`.
- `brain/` — planning artefacts (not used by the runtime).

## Requirements
- Java 17 (JDK)
- Maven 3.9+
- MySQL 8.x

## Quick start (dev)
1) Start MySQL and ensure a user with create rights exists (default config uses `root`/empty password on `localhost:3306`).
2) Copy `code/backend/src/main/resources/application-dev.properties` (added in this repo) and adjust `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` if needed.
3) Run from repo root:
```bash
cd code/backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
4) App serves at http://localhost:8080/ (context path `/`).

## Seeded data & default logins
On first boot (when tables are empty) `DataSeeder` inserts sample data:
- Tables: Pool/Carom/VIP tables with default statuses
- Price rules for weekday/weekend/holiday by time ranges
- Users: `admin/admin123`, `staff1/staff123`, `staff2/staff123`
- App settings, sample products/drinks, holiday calendar, sample customers, sample shifts

## Profiles & configuration
- Common defaults live in `src/main/resources/application.properties`.
- `application-dev.properties` (committed): convenient local profile with `ddl-auto=update`, SQL logging enabled. Activate with `--spring.profiles.active=dev`.
- `application-prod.properties.example` (committed): safer defaults (`ddl-auto=validate`, SQL logging off). Copy to `application-prod.properties`, set real credentials/secrets, and run with `--spring.profiles.active=prod`.
- You can also override via environment variables (see property placeholders inside the files).

## Static assets
Templates pull Bootstrap and icons from CDN. Put any custom JS/CSS/images under `src/main/resources/static/` (already configured as `classpath:/static/`).

## Build & test
- Run tests: `mvn test`
- Package jar: `mvn package`
- CI: GitHub Actions workflow `backend-ci.yml` builds and tests the Maven project (added in `.github/workflows`).

## Folder map (backend)
- `src/main/java/com/bida` — business code (`billing`, `controller`, `service`, `repository`, `entity`, `websocket`, `config`)
- `src/main/resources/templates` — Thymeleaf pages (dashboard, admin screens)
- `src/main/resources/static` — place for CSS/JS assets
- `src/test/java/com/bida` — tests (current coverage focuses on billing calculator)

## Notes & next steps
- Consider splitting sensitive settings into an untracked `application-secret.properties` (already ignored) and using environment variables for DB credentials.
- Expand test coverage for controllers/services/repositories and add integration tests hitting an in-memory database (e.g., Testcontainers/MySQL or H2 with MySQL dialect).
