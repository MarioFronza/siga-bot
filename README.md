# siga-bot

A small bot that logs into [SIGA](https://siga.udesc.br) (UDESC's academic system) on behalf of a student, scrapes their semester grades, stores them, and sends a push notification when a new grade shows up. A background job re-checks every registered student every hour.

## Technologies

- Kotlin 2.4, Java 25
- Spring Boot 4 (Web MVC, Security, jOOQ, Flyway)
- PostgreSQL
- Retrofit + OkHttp and Jsoup for scraping SIGA
- JWT auth (auth0 java-jwt)
- OneSignal for push notifications
- Kotest + MockK for tests
- Gradle, Docker Compose

## Running

Copy the env template and fill in the secrets:

```bash
cp .env.example .env
```

| Variable | Description |
| --- | --- |
| `DATASOURCE_USERNAME` / `DATASOURCE_PASSWORD` | Postgres credentials |
| `JWT_SECRET` | at least 32 bytes, e.g. `openssl rand -base64 48` |
| `SECRETKEY` | exactly 32 chars, used to encrypt SIGA credentials, e.g. `openssl rand -hex 16` |
| `ONESIGNAL_APIKEY` | OneSignal REST API key |
| `ADMIN_EMAILS` | optional, comma-separated. Listed users get the `ADMIN` role at startup, and listed emails get it at sign-up |
| `SIGA_YEAR` / `SIGA_TERM` | optional. By default the hourly job syncs the most recent period SIGA lists, so nothing changes across a semester boundary. Set both to pin a period, e.g. `2026` / `1` |

With Docker Compose, after building the distribution the image packages:

```bash
./gradlew :application:distTar
docker compose up --build
```

The API listens on `http://localhost:8080`.

Without the app container (needs Java 25 and a running Postgres):

```bash
export $(grep -v '^#' .env | xargs)
export DATASOURCE_URL=jdbc:postgresql://localhost:5432/sigabot
./gradlew :application:installDist
application/build/install/application/bin/application
```

Run the tests:

```bash
./gradlew test
```

## Release

Every pull request runs the Gradle build and tests, builds the Docker image, scans it with Trivy (failing on fixable `HIGH` or `CRITICAL` vulnerabilities), and starts it against Postgres 18, failing unless the container reports `healthy`. It also checks that the title follows [Conventional Commits](https://www.conventionalcommits.org), because the squash commit title drives the release. All of these checks must pass before merging into `main`.

After the same checks pass on `main`, every push publishes `mariofronza/siga-bot:sha-<short>` to Docker Hub.

Releases come from [release-please](https://github.com/googleapis/release-please). Each push to `main` opens or updates a `chore(main): release X.Y.Z` pull request, with the version bump and `CHANGELOG.md` derived from the conventional commits since the last release. Merging that pull request creates the `vX.Y.Z` tag and GitHub release, and the tag publishes `:X.Y.Z`, `:X.Y` and `:latest`, so `latest` always points at the newest release.

Dependabot opens weekly pull requests for Gradle dependencies, the Docker base image and GitHub Actions. Dependency bumps use `fix(deps)` titles, so merging one ships a patch release.

The workflow needs a `DOCKERHUB_USERNAME` variable plus `DOCKERHUB_TOKEN` and `RELEASE_PLEASE_TOKEN` secrets in the repository settings. `RELEASE_PLEASE_TOKEN` is a fine-grained token for this repository with read and write access to contents and pull requests. The default `GITHUB_TOKEN` does not work here because pull requests and tags it creates do not trigger the `build` check or the image publish.

To roll back, run the previous `X.Y.Z` tag against the same database.

## Running the published image

The image needs the variables above plus `DATASOURCE_URL` pointing at Postgres. It listens on `8080` and reports readiness through `/actuator/health/readiness`, which also checks the database, so `docker ps` shows `healthy` once it can serve.

```bash
docker run -d --name siga-bot --restart unless-stopped --env-file .env \
  -e DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/sigabot \
  -p 8080:8080 mariofronza/siga-bot:latest
```

[Watchtower](https://containrrr.dev/watchtower/) on the same host picks up each new `latest` and restarts the container, so merging a release pull request is the whole deploy.

If a variable is missing, the container stops with `Missing required environment variables: ...`.

To expose it over HTTPS, point a Cloudflare Tunnel route at `http://<host>:8080`.

## Grade sync

Every student has a row in `sync_jobs` with the time of their next sync. Each instance keeps 8 sync slots. Every 5 seconds it claims as many due jobs as it has free slots, with `FOR UPDATE SKIP LOCKED` and a 10-minute lease, and syncs each one on a virtual thread. A slow SIGA answer holds only its own slot, and the others keep claiming work. Running several instances is safe, because a job claimed by one is invisible to the others until its lease ends.

- A successful sync schedules the next one an hour later, which spreads the load across the hour instead of hitting SIGA at the top of it.
- A failed sync retries after 5 minutes, doubling up to 6 hours, and keeps the error in `last_error`.
- A new student is synced within a minute of signing up.

Pushes go through an outbox. New-grade and admin messages are written in the same transaction as the data they describe. Every 10 seconds each instance claims up to 50 pending pushes with `FOR UPDATE SKIP LOCKED` and a 5-minute lease, so two instances never send the same push. A failed push retries after 30 seconds, doubling up to an hour, for up to 10 attempts (about 4.5 hours). A OneSignal outage no longer loses pushes, and nothing is sent for data that was rolled back. Delivery is at least once: a crash between OneSignal accepting a push and marking it delivered sends it again.

Prometheus metrics: `siga_sync_duration_seconds`, `siga_sync_failures_total{reason}`, `siga_sync_queue_due` and `siga_outbox_pending`.

## Database

[Flyway](https://documentation.red-gate.com/flyway) creates and migrates the schema on startup from `persistence/src/main/resources/db/migration`, and the build generates the [jOOQ](https://www.jooq.org) classes from those same migrations. It applies them to a throwaway Postgres 18 container, so a query that no longer matches the schema fails to compile. Building `persistence` therefore needs Docker, the same as its tests. The generation is skipped while the migrations are unchanged.

- Add a new `V<n>__<description>.sql` for every schema change. Never edit a migration that already ran, because Flyway checks their checksums.
- `./gradlew test` runs the repository tests against a throwaway Postgres 18 through Testcontainers, so Docker must be running.
- Flyway only takes over an empty database. It refuses to start on a schema created before it, with `Found non-empty schema(s) "public" but no schema history table`.

Back up and restore Postgres:

```bash
docker exec <db-container> pg_dump -U sigabot sigabot > siga-bot.sql
docker exec -i <db-container> psql -U sigabot sigabot < siga-bot.sql
```

## Endpoints

The API is versioned in the path, under `/api/v1`. A version the server does not serve answers `400`. Protected routes need `Authorization: Bearer <accessToken>`. Access tokens last 10 minutes. Refresh tokens are opaque, stored only as a SHA-256 hash, and rotate on every refresh with a 30-day lifetime. Sign-up, login and refresh are each limited to 10 requests per minute per client address, and going over answers `429`. Behind a reverse proxy on a private network, the address comes from `X-Forwarded-For`.

The OpenAPI contract is served at `GET /api/v1/openapi.json`. It covers every route except `POST /api/v1/sessions`, which Spring Security handles before MVC.

Errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) and come as `application/problem+json`:

```json
{ "status": 409, "title": "Conflict", "detail": "User already exists", "instance": "/api/v1/users" }
```

| Route | Access | Purpose |
|---|---|---|
| `GET /actuator/health/liveness`, `/actuator/health/readiness` | public | container probes, outside the versioned API |
| `GET /actuator/prometheus` | public | metrics for Prometheus |
| `POST /api/v1/users` | public | sign up |
| `POST /api/v1/sessions` | public | log in |
| `POST /api/v1/sessions/refresh` | public | trade a refresh token for new tokens |
| `POST /api/v1/sessions/revoke` | public | log out |
| `GET /api/v1/me` | authenticated | the signed-in user |
| `GET /api/v1/me/terms` | `STUDENT` | terms with grades, newest first |
| `GET /api/v1/me/terms/{year}/{term}/enrollments` | `STUDENT` | stored grades of a term |
| `POST /api/v1/me/syncs` | `STUDENT` | read SIGA now and store the grades |
| `GET /api/v1/me/notifications` | authenticated | the user's notifications, paginated |
| `POST /api/v1/notifications` | `ADMIN` | push a message to every device |
| `POST /api/v1/users/{id}/notifications` | `ADMIN` | push a message to one user and store it |

### `POST /api/v1/users`

```json
{
  "email": "student@example.com",
  "password": "app-password",
  "sigaCredential": {
    "cpf": "00000000000",
    "password": "siga-password"
  }
}
```

Response `201` with `Location: /api/v1/me`:

```json
{ "id": "01a1f973-13ad-7a5c-80df-bf421ada5df1", "email": "student@example.com", "fullName": "" }
```

`id` is the public user id, a UUIDv7. The email is stored in lowercase, and a CPF can only be registered once, whatever its formatting. Both duplicates answer `409`, and an invalid body answers `400`. `fullName` stays empty until the first sync reads it from SIGA. `GET /api/v1/me` returns the same shape.

### `POST /api/v1/sessions`

```json
{ "email": "student@example.com", "password": "app-password" }
```

Response `200` (`401` on wrong credentials):

```json
{ "accessToken": "eyJ...", "refreshToken": "q1Vb...", "userId": "01a1f973-13ad-7a5c-80df-bf421ada5df1" }
```

The app registers `userId` as the OneSignal external user id, which is how user notifications and new-grade pushes reach the device.

### `POST /api/v1/sessions/refresh`

```json
{ "refreshToken": "q1Vb..." }
```

Response `200` has the same body as `/sessions`, with a new refresh token. The one sent is spent: using it again revokes the whole session, so a stolen token works at most until its owner refreshes. It answers `401` for an unknown, expired, reused or revoked token.

### `POST /api/v1/sessions/revoke`

Logs out. Same body as `/sessions/refresh`. It answers `204` and revokes every refresh token of that session.

### `GET /api/v1/me/terms`

```json
[{ "year": 2026, "term": 2 }, { "year": 2026, "term": 1 }]
```

### `GET /api/v1/me/terms/{year}/{term}/enrollments`

```json
[
  {
    "subject": "Banco de Dados",
    "classSection": "BAN01",
    "courseLoad": 72,
    "finalGrade": 8.0,
    "absences": 0,
    "attendancePercentage": 100.0,
    "result": "Aprovado"
  }
]
```

### `POST /api/v1/me/syncs`

Logs into SIGA, stores the grades, and notifies about any new grade. Sync one term with `{ "year": 2026, "term": 1 }`, or every term SIGA has with `{ "scope": "full" }`. It answers `200` with the synced terms, in the same shape as `/me/terms`, and `400` when `year` or `term` is missing.

### `GET /api/v1/me/notifications?page=0&size=20`

Newest first. `size` defaults to 20 and is capped at 100.

```json
{
  "content": [{ "id": 7, "message": "Grades are out", "createdAt": "2026-11-20T12:00:00Z" }],
  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
}
```

### `POST /api/v1/notifications` and `POST /api/v1/users/{id}/notifications`

```json
{ "message": "Grades are out" }
```

Broadcasting answers `202` with an empty body. Notifying one user, by public id, stores the notification and answers `201` with it, or `404` when the user does not exist.
