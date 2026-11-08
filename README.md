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
