# Task manager backend

Spring Boot (Java 21) REST API for caseworker tasks, with a PostgreSQL db.

## Running

Start Postgres with Docker from this `backend/` directory, then run the app:

```
docker compose up -d
./gradlew bootRun
```

The API runs on http://localhost:4000.
Flyway creates the schema on startup from `src/main/resources/db/migration`.

The connection can be changed with environment variables:

| Variable       | Default     |
| -------------- | ----------- |
| `DB_HOST`      | `localhost` |
| `DB_PORT`      | `5432`      |
| `DB_NAME`      | `tasks`     |
| `DB_USER_NAME` | `tasks`     |
| `DB_PASSWORD`  | `tasks`     |

To look at the data: `docker compose exec postgres psql -U tasks -d tasks` or check the database tab of Intellij.

## API

Swagger UI: http://localhost:4000/swagger-ui.html (OpenAPI JSON at `/v3/api-docs`).

| Method | Path                 | Body                          | Success             |
| ------ | -------------------- | ----------------------------- | ------------------- |
| POST   | `/tasks`             | Task (below)                  | 201 + created task  |
| GET    | `/tasks`             |                               | 200 + list of tasks |
| GET    | `/tasks/{id}`        |                               | 200 + task          |
| PATCH  | `/tasks/{id}/status` | `{ "status": "IN_PROGRESS" }` | 200 + updated task  |
| DELETE | `/tasks/{id}`        |                               | 204                 |

Sample Task request body:

```json
{
  "title": "Review case bundle",
  "description": "Optional, up to 3000 characters",
  "status": "TODO",
  "dueDate": "2026-12-01T10:00"
}
```

- `title` is required, up to 255 characters.
- `status` is required: enum values are `TODO`, `IN_PROGRESS` or `COMPLETED`.
- `dueDate` is required, as a local date-time.

Responses have the same fields plus `id`.

### Errors

Errors use the `ProblemDetail` format. Validation failures also include an `errors` map of field to message:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/tasks",
  "errors": { "title": "must not be blank" }
}
```

## Tests

```
./gradlew build
```

This runs checkstyle, the unit tests and the integration tests. The integration
tests use in-memory H2 in PostgreSQL mode, so Postgres doesn't need to be running.
