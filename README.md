# EndoNutri

A fresh React + TypeScript / Java Spring Boot / PostgreSQL application foundation for endometriosis health tracking and research.

## Included in this delivery

- React/Vite/TypeScript responsive role-aware application shell.
- Java 21 + Spring Boot REST API.
- PostgreSQL and Flyway schema migrations.
- JWT authentication, BCrypt password hashing, role-based API authorization.
- Patient symptom record CRUD API and patient UI.
- Explicit research-consent API and a de-identified research export endpoint with a minimum cohort threshold.
- Research CSV upload endpoint with file-size/type checks and dataset metadata.
- Java descriptive-statistics endpoint (count, mean, median, min, max, standard deviation).
- Docker Compose for PostgreSQL.
- Environment configuration examples and run instructions.

## Not yet production-complete

The request describes a large healthcare/research platform. This delivery is a runnable foundation and first vertical slice, not a claim that every requested module has been implemented or clinically validated. Consultation messaging, full nutrition planning, external scholarly search, advanced statistical tests, clustering/PCA, report PDF generation, admin workflows, password reset delivery, offline synchronization, and comprehensive automated end-to-end testing remain follow-on implementation work. Do not use with real patient data until security, privacy, clinical, legal, and operational reviews are completed.

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop / Docker Engine

## Start PostgreSQL

```bash
docker compose up -d db
```

Default local database values are in `.env.example`. For local development, copy it to `.env` and export the values into your shell, or set them in your IDE run configuration.

```bash
cp .env.example .env
```

## Local development: start PostgreSQL\n\n```bash\ndocker compose up -d db\n```\n\n## Start backend

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/endonutri
export DB_USERNAME=endonutri
export DB_PASSWORD=change-me-local
export JWT_SECRET=replace-this-with-a-random-secret-at-least-32-characters
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`

## Start frontend

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Register and sign in

Use the registration form. Public registration supports `PATIENT`, `RESEARCHER`, and `ADVISOR`. `ADMIN` registration is intentionally rejected; provision administrators through a controlled database/operations process after deployment.

## Main API routes

| Method | Route | Access |
|---|---|---|
| POST | `/api/auth/register` | Public (non-admin roles) |
| POST | `/api/auth/login` | Public |
| GET | `/api/patient/symptoms` | Patient |
| POST | `/api/patient/symptoms` | Patient |
| DELETE | `/api/patient/symptoms/{id}` | Patient, own record |
| PUT | `/api/patient/research-consent` | Patient |
| GET | `/api/research/consented-symptoms` | Researcher, cohort threshold enforced |
| POST | `/api/research/datasets` | Researcher, CSV multipart upload |
| POST | `/api/research/statistics/descriptive` | Researcher |

## Environment variables

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC PostgreSQL URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | HMAC JWT secret, at least 32 characters |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins |
| `UPLOAD_DIR` | Dataset upload directory |
| `MIN_RESEARCH_COHORT` | Minimum consenting patients before research export |
| `VITE_API_BASE_URL` | Frontend API base URL |

## Important privacy notes

The research export returns aggregated symptom observations without direct identifiers and enforces a minimum number of distinct consenting patients. This is a starter safeguard, not a formal anonymization guarantee. Small-cell suppression, differential privacy, threat modelling, data retention policy, consent versioning, encryption-at-rest, audit review, and jurisdiction-specific compliance must be completed before production.

## Project layout

```text
endonutri/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/endonutri/
│       │   ├── EndoNutriApplication.java
│       │   ├── config/
│       │   ├── security/
│       │   ├── auth/
│       │   ├── patient/
│       │   └── research/
│       └── resources/
│           ├── application.yml
│           └── db/migration/V1__initial_schema.sql
├── frontend/
│   ├── package.json
│   ├── index.html
│   └── src/
│       ├── api.ts
│       ├── main.tsx
│       ├── App.tsx
│       └── styles.css
├── docker-compose.yml
└── .env.example
```

## Development notes

- Flyway owns schema evolution; do not edit an applied migration. Add a new numbered migration.
- The API stores passwords using BCrypt.
- JWT tokens are sent as Bearer tokens. For a hardened deployment, prefer short-lived access tokens plus rotating, HttpOnly, Secure, SameSite refresh cookies and CSRF protections where applicable.
- Dataset files should be stored outside the web root and scanned before analysis.
- Configure HTTPS, secrets management, backups, rate limiting, monitoring, and a production-grade object store before deployment.
