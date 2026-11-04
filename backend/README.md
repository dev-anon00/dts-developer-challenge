# HMCTS Dev Test Backend

This will be the backend for the brand new HMCTS case management system. As a potential candidate we are leaving
this in your hands. Please refer to the brief for the complete list of tasks! Complete as much as you can and be
as creative as you want.

You should be able to run `./gradlew build` to start with to ensure it builds successfully. Then from that you
can run the service in IntelliJ (or your IDE of choice) or however you normally would.

There is an example endpoint provided to retrieve an example of a case. You are free to add/remove fields as you
wish.

## Database

The service uses PostgreSQL. Tests use an in-memory H2 database, so `./gradlew build` doesn't need Postgres.

Start Postgres with Docker from this `backend/` directory:

```
docker compose up -d
```

Then run `./gradlew bootRun`.
The connection defaults to `localhost:5432`, with database, user and password all `tasks`.
