# Caseworker task manager

My submission for the HMCTS DTS developer technical test: a small system that lets caseworkers create, view, update
and delete their tasks.

| Directory               | What it is                                                                    |
| ----------------------- | ----------------------------------------------------------------------------- |
| [`backend/`](backend)   | Spring Boot (Java 21) REST API, PostgreSQL + Flyway, validation, OpenAPI docs |
| [`frontend/`](frontend) | React + TypeScript (Vite) single page, styled with the GOV.UK Design System   |

## Running locally

Prerequisites: JDK 21, Docker, and Node 22 with Corepack enabled (`corepack enable`) for Yarn.

```
# 1. Database (from backend/)
cd backend
docker compose up -d

# 2. API on http://localhost:4000
./gradlew bootRun

# 3. Frontend on http://localhost:3100
cd frontend
yarn install
yarn dev
```

## API

| Method | Path                 | Description          |
| ------ | -------------------- | -------------------- |
| POST   | `/tasks`             | Create a task        |
| GET    | `/tasks`             | List all tasks       |
| GET    | `/tasks/{id}`        | Get a task by ID     |
| PATCH  | `/tasks/{id}/status` | Update a task status |
| DELETE | `/tasks/{id}`        | Delete a task        |

Interactive docs are at http://localhost:4000/swagger-ui.html, these require the backend to be running.

## Tests

- Backend: `cd backend && ./gradlew build` runs checkstyle, unit and integration tests (no Postgres required).
- Frontend: `cd frontend && yarn lint && yarn test && yarn build`.

## Design

- **PostgreSQL + Flyway** for storage, with the schema in a versioned migration. Tests run against in-memory H2 in
  PostgreSQL mode, and Hibernate validates the schema against the entity.
- **Validation** happens on both sides: Bean Validation on the request DTOs, with matching checks in the form for
  quicker feedback. Backend field errors are still shown in the form if they get through.
- **Errors** are returned as `ProblemDetail` JSON from a single `GlobalExceptionHandler`.
- **GOV.UK Design System** CSS keeps the UI simple.
