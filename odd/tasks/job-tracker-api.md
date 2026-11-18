# job-tracker-api

Objective: personal, realistic Spring Boot REST API to track job applications and the exact CV version sent to each one; public portfolio project.
Branch: main (bootstrap), feature branches per task afterwards · Delivery: ask-on-risk · Test runner: `./mvnw test` (JUnit 5 + Testcontainers PostgreSQL)

## Specs

- S1 — Realistic and useful project: "me gustaría crear un pryecto realista que me pudiese proporcional alguna utilidad. Aunque sea a futuro."
- S2 — Track applications: companies, job offers, applications (status, sent date, cover letter), interviews and follow-up reminders.
- S3 — Multiple CVs: "también podría gestionar múltiples CV en PDF, con n perfiles (ampliables) QA, BBDD,backend.NET, backend.NET-Java, etc."
- S4 — Traceability: "Necesito sabe que curriculum envié a cada candidatura." An application references one immutable CV version (PDF + SHA-256), never only the profile.
- S5 — Repository: "en una carpeta propia con repositorio público  en GitHub", English naming ("en inglés que cada vez gana más peso en las ofertas en España").
- S6 — Stack (assumption, agreed in conversation): Java 17, Spring Boot 4.1.2, Maven, PostgreSQL, Flyway, Spring Security (JWT), JUnit/Mockito/Testcontainers, Docker, GitHub Actions.

## Tasks

| ID | Specs | Route | Status | Evidence |
|----|-------|-------|--------|----------|
| T1 | S5, S6 | inline | done | scaffold; `./mvnw test` green (context loads on PostgreSQL 18 Testcontainers) |
| T2 | S5 | inline | done | https://github.com/domgarlor/job-tracker-api (bootstrap e38e731, noreply author email) |
| T3 | S3, S4 | inline | done | CV profiles + immutable CV versions; RED 10/11 failing, GREEN 12/12 (`CvApiTests`) on branch feat/cv-profiles-versions |
| T4 | S2, S4 | inline | pending | companies, offers, applications referencing a CV version |
| T5 | S2 | inline | pending | interviews and follow-ups with `@Scheduled` reminders |
| T6 | S6 | inline | pending | Spring Security with JWT |
| T7 | S6 | inline | pending | Docker Compose, GitHub Actions CI, README |

## Log

- L1 (2026-11-18, user): "me gustaría crear un pryecto realista que me pudiese proporcional alguna utilidad. Aunque sea a futuro."
- L2 (2026-11-18, user): "también podría gestionar múltiples CV en PDF, con n perfiles (ampliables) QA, BBDD,backend.NET, backend.NET-Java, etc. Necesito sabe que curriculum envié a cada candidatura."
- L3 (2026-11-18, user): "en una carpeta propia con repositorio público  en GitHub" / "en inglés que cada vez gana más peso en las ofertas en España."
- L4: Purpose: defend Spring Boot in interviews (real Java experience is test automation); built and tested without relying on AI in live tests.
- L5: T1 done. Initializr returned `4.1.2.RELEASE` as parent version; Maven Central uses `4.1.2` — fixed in pom.xml. Next: T2.
- L6 (2026-11-18): T2 done. User chose GitHub noreply email for commits in this repo ("sí, usa la dirección privada de GitHub"). Next: T3.
- L7 (2026-11-18): T3 done. User: "escribe el código. Después lo reviso y te pregunto lo que no entienda." Files stored on disk keyed by SHA-256 (app.storage.cv-dir); versions immutable (@Immutable, no PUT/DELETE); duplicate PDF -> 409 with existingVersionId; non-PDF (magic bytes) -> 415. ddl-auto=validate caught CHAR vs VARCHAR mismatch. Next: T4.
- L8 (2026-11-18): RDD on (default). `gentle-ai review assess --base-ref main --committed-only` on 280e933: risk medium, review_due false (under_budget) — slice stays pending, reviewed boundary remains main. gentle-ai 4.5.0 lives in /home/linuxbrew/.linuxbrew/bin (not in Claude Code PATH).
