# siga-bot

A small bot that logs into [SIGA](https://siga.udesc.br) (UDESC's academic system) on behalf of a student, scrapes their semester grades, stores them, and sends a push notification when a new grade shows up. A background job re-checks every registered student every hour.

## Technologies

- Kotlin 2.4, Java 21
- Spring Boot 4 (Web MVC, Security, Data JPA)
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
| `SIGA_YEAR` / `SIGA_TERM` | period the hourly job fetches, e.g. `2020` / `2` |

With Docker:

```bash
docker compose up --build
```

The API listens on `http://localhost:8080`.

Without Docker (needs Java 21 and a running Postgres):

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

After the build and tests pass, every push to `main` publishes `mariofronza/siga-bot:latest` and `:sha-<short>` to Docker Hub.

Releases come from [release-please](https://github.com/googleapis/release-please). Each push to `main` opens or updates a `chore(main): release X.Y.Z` pull request, with the version bump and `CHANGELOG.md` derived from the conventional commits since the last release. Merging that pull request creates the `vX.Y.Z` tag and GitHub release, and the tag publishes `:X.Y.Z` and `:X.Y`.

The workflow needs a `DOCKERHUB_USERNAME` variable plus `DOCKERHUB_TOKEN` and `RELEASE_PLEASE_TOKEN` secrets in the repository settings. `RELEASE_PLEASE_TOKEN` is a fine-grained token for this repository with read and write access to contents and pull requests. The default `GITHUB_TOKEN` does not work here because pull requests and tags it creates do not trigger the `build` check or the image publish.

To roll back, run the previous `X.Y.Z` tag against the same database.

## Running the published image

The image needs the variables above plus `DATASOURCE_URL` pointing at Postgres. It listens on `8080` and reports health through `/health`, so `docker ps` shows `healthy` once it is up. Run a single instance, because the hourly job logs into SIGA for every student.

```bash
docker run -d --name siga-bot --restart unless-stopped --env-file .env \
  -e DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/sigabot \
  -p 8080:8080 mariofronza/siga-bot:latest
```

If a variable is missing, the container stops with `Missing required environment variables: ...`.

To expose it over HTTPS, point a Cloudflare Tunnel route at `http://<host>:8080`.

Back up and restore Postgres:

```bash
docker exec <db-container> pg_dump -U sigabot sigabot > siga-bot.sql
docker exec -i <db-container> psql -U sigabot sigabot < siga-bot.sql
```

## Endpoints

Protected routes need `Authorization: Bearer <accessToken>`. Access tokens last 10 minutes and refresh tokens last 1 year.

### `GET /health`

Public. Response `200`:

```json
true
```

### `POST /users`

Public. Signs up a student; every new user gets the `STUDENT` role.

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

Response `201`:

```json
{ "id": 1, "username": "", "email": "student@example.com" }
```

`username` stays empty until the first retrieve reads the name from SIGA.

### `POST /sessions`

Public. Logs in.

```json
{ "email": "student@example.com", "password": "app-password" }
```

Response `200` (`401` on wrong credentials):

```json
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "userId": "1" }
```

### `POST /auth/refresh-token`

Header `Authorization: Bearer <refreshToken>`. Response `200` has the same body as `/sessions`. Returns `401` when the token is invalid and `400` when the header is missing.

### `GET /retrieve?year=2020&term=2`

`STUDENT`. Logs into SIGA, scrapes that period, saves the grades, and notifies about any new grade.

Response `200`:

```json
{
  "username": "Ana",
  "periods": [{ "id": 1, "year": "2020", "term": 2 }]
}
```

### `GET /retrieve/full`

`STUDENT`. Same as above for every period available in SIGA. Response `200` has the same shape, with all periods.

### `GET /dashboard/info`

`STUDENT`. Lists the periods already stored for the user.

```json
{
  "username": "Ana",
  "periods": [
    { "id": 1, "year": "2020", "term": 2 },
    { "id": 2, "year": "2020", "term": 1 }
  ]
}
```

### `GET /dashboard?year=2020&term=2`

`STUDENT`. Stored grades for a period.

```json
{
  "period": { "id": 1, "year": "2020", "term": 2 },
  "results": [
    {
      "finalGrade": 8.0,
      "courseLoad": 72,
      "absencesCount": 0,
      "attendancePercentage": 100.0,
      "result": "Aprovado",
      "subjectName": "Banco de Dados",
      "groupName": "BAN01"
    }
  ]
}
```

### `GET /notifications`

Authenticated. The current user's notifications.

```json
[{ "description": "Ana, você possui uma nota nova da matéria Banco de Dados", "entityId": 1 }]
```

### `POST /notifications/all` and `POST /notifications/user/{id}`

`ADMIN`. `/all` pushes a message to every device; `/user/{id}` pushes it to one user and stores it.

```json
{ "description": "Grades are out", "entityId": 1 }
```

Response `200` with an empty body.
