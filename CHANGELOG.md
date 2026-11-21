# Changelog

## [3.3.0](https://github.com/MarioFronza/siga-bot/compare/v3.2.0...v3.3.0) (2026-11-21)


### Features

* detect a login SIGA rejects ([#103](https://github.com/MarioFronza/siga-bot/issues/103)) ([19fbe0e](https://github.com/MarioFronza/siga-bot/commit/19fbe0ee7293ac35bfced27f112660669f232931))
* pause syncing when SIGA rejects the credential ([#99](https://github.com/MarioFronza/siga-bot/issues/99)) ([27d2890](https://github.com/MarioFronza/siga-bot/commit/27d28906c31378ab5c53d8bbd26f0f491afd500a))
* rate limit the routes that trigger SIGA logins ([#100](https://github.com/MarioFronza/siga-bot/issues/100)) ([251a433](https://github.com/MarioFronza/siga-bot/commit/251a4332d7adf4d784d40385bff28b91e2119b6a))
* tell why a sync failed instead of exposing the raw error ([#98](https://github.com/MarioFronza/siga-bot/issues/98)) ([5c246bc](https://github.com/MarioFronza/siga-bot/commit/5c246bce88a7d90a30012b0144f9ed6a3a9eef67))

## [3.2.0](https://github.com/MarioFronza/siga-bot/compare/v3.1.1...v3.2.0) (2026-11-21)


### Features

* expose the student's sync status ([#87](https://github.com/MarioFronza/siga-bot/issues/87)) ([4629b1a](https://github.com/MarioFronza/siga-bot/commit/4629b1ad7d905520c6260e6503355bf3c0cc0147))
* expose the user's roles on /me ([#81](https://github.com/MarioFronza/siga-bot/issues/81)) ([2abc89a](https://github.com/MarioFronza/siga-bot/commit/2abc89ae9211233dad668c0f4d53f17f28708463))
* let admins search students ([#89](https://github.com/MarioFronza/siga-bot/issues/89)) ([473f78a](https://github.com/MarioFronza/siga-bot/commit/473f78a2d4e2e6d6dc8873755294e04021e50f31))
* let students update their SIGA credential ([#90](https://github.com/MarioFronza/siga-bot/issues/90)) ([945f7da](https://github.com/MarioFronza/siga-bot/commit/945f7dabfb305570adc85bb7980f2162c4dc3e4c))
* list the invalid fields of a bad request ([#83](https://github.com/MarioFronza/siga-bot/issues/83)) ([b195567](https://github.com/MarioFronza/siga-bot/commit/b195567ace123c0fac85a414b824a3bfc466466c))
* return the recipient of a direct notification ([#91](https://github.com/MarioFronza/siga-bot/issues/91)) ([d01f90c](https://github.com/MarioFronza/siga-bot/commit/d01f90c1099ac6fcc6292f0fd1134a8de3576518))
* run requested syncs in the background ([#88](https://github.com/MarioFronza/siga-bot/issues/88)) ([b0dd465](https://github.com/MarioFronza/siga-bot/commit/b0dd465a4625fe20da111a8909f3d98e1d9ed3b0))
* tell grade notifications from announcements ([#86](https://github.com/MarioFronza/siga-bot/issues/86)) ([df9ae96](https://github.com/MarioFronza/siga-bot/commit/df9ae96265b7fac44e7d045e3c4bb3cb97d87bb6))
* tell rate-limited clients when to retry ([#84](https://github.com/MarioFronza/siga-bot/issues/84)) ([c9456de](https://github.com/MarioFronza/siga-bot/commit/c9456deb897f65f5a6bce6533a4976ada6699a1d))
* tell which field a sign-up conflict is about ([#82](https://github.com/MarioFronza/siga-bot/issues/82)) ([05fe803](https://github.com/MarioFronza/siga-bot/commit/05fe803fda87fe749854ed2cbe16208bfbf0ec25))


### Bug Fixes

* cap notification messages at 500 characters ([#85](https://github.com/MarioFronza/siga-bot/issues/85)) ([05bbac4](https://github.com/MarioFronza/siga-bot/commit/05bbac4d61da5c443044fba779b888937f971577))
* keep grades empty while a subject is in progress ([#80](https://github.com/MarioFronza/siga-bot/issues/80)) ([d2ba894](https://github.com/MarioFronza/siga-bot/commit/d2ba894150901cb9aee17c30917d4471cc947b96))

## [3.1.1](https://github.com/MarioFronza/siga-bot/compare/v3.1.0...v3.1.1) (2026-11-21)


### Bug Fixes

* treat a push OneSignal rejects as failed ([#60](https://github.com/MarioFronza/siga-bot/issues/60)) ([e3c4245](https://github.com/MarioFronza/siga-bot/commit/e3c42451acf10585f0fd85d9357dc80f3d77b49b))


### Performance Improvements

* bound SIGA and OneSignal calls with timeouts ([#65](https://github.com/MarioFronza/siga-bot/issues/65)) ([3649b19](https://github.com/MarioFronza/siga-bot/commit/3649b19dfacd8999971d6dc31fd368be28bd872a))
* keep every sync slot busy instead of waiting for the slowest of a batch ([#63](https://github.com/MarioFronza/siga-bot/issues/63)) ([c669fbf](https://github.com/MarioFronza/siga-bot/commit/c669fbf481e702b99eda4fc48e47b2c8e691f35e))
* run scheduled jobs and requests on virtual threads and size the JVM heap ([#61](https://github.com/MarioFronza/siga-bot/issues/61)) ([ae23e69](https://github.com/MarioFronza/siga-bot/commit/ae23e69603f62b75ea0bffc42063f959be4d8365))
* write nothing when a sync finds nothing new ([#64](https://github.com/MarioFronza/siga-bot/issues/64)) ([10c7a7e](https://github.com/MarioFronza/siga-bot/commit/10c7a7e75937e614ae7e9d3353d0846ee3e359af))

## [3.1.0](https://github.com/MarioFronza/siga-bot/compare/v3.0.0...v3.1.0) (2026-11-20)


### Features

* rotate and revoke refresh tokens, rate limit auth and bootstrap admins ([#53](https://github.com/MarioFronza/siga-bot/issues/53)) ([224a7a0](https://github.com/MarioFronza/siga-bot/commit/224a7a05629523664c85f171a6432f81bbef0618))
* scale the grade sync with a job queue and an outbox ([#52](https://github.com/MarioFronza/siga-bot/issues/52)) ([21a6754](https://github.com/MarioFronza/siga-bot/commit/21a6754bd0670028dbe9baea31047f21f8e3b692))
* sync the latest period SIGA lists instead of a configured one ([#51](https://github.com/MarioFronza/siga-bot/issues/51)) ([8d3b2d6](https://github.com/MarioFronza/siga-bot/commit/8d3b2d6f83c22d54d9900785128fd76253774c43))

## [3.0.0](https://github.com/MarioFronza/siga-bot/compare/v2.0.1...v3.0.0) (2026-11-20)


### ⚠ BREAKING CHANGES

* every route except /health moved under /api/v1, the refresh token now goes in the request body, and several JSON fields were renamed. The app has to move to the new contract.

### Features

* serve the api under /api/v1 with resource-oriented routes ([#33](https://github.com/MarioFronza/siga-bot/issues/33)) ([d55bf18](https://github.com/MarioFronza/siga-bot/commit/d55bf18a102eb3429b1c6cb0c69821c76027f644))

## [2.0.1](https://github.com/MarioFronza/siga-bot/compare/v2.0.0...v2.0.1) (2026-11-20)


### Bug Fixes

* keep the hourly retrieve going when one student fails ([#29](https://github.com/MarioFronza/siga-bot/issues/29)) ([ba0b9ff](https://github.com/MarioFronza/siga-bot/commit/ba0b9ff5ee747f0584545df63ecc51a7aef62914))

## [2.0.0](https://github.com/MarioFronza/siga-bot/compare/v1.0.1...v2.0.0) (2026-11-20)


### ⚠ BREAKING CHANGES

* the database must be recreated, because Flyway refuses to start on a schema it did not create. API responses no longer include internal period ids, year is an integer, and notifications drop entityId.

### Features

* manage the schema with flyway and redesign the data model ([#26](https://github.com/MarioFronza/siga-bot/issues/26)) ([1f97e22](https://github.com/MarioFronza/siga-bot/commit/1f97e223db07568b6879ce48b8035070c5ce02c6))

## [1.0.1](https://github.com/MarioFronza/siga-bot/compare/v1.0.0...v1.0.1) (2026-11-20)


### Bug Fixes

* **deps:** bump the gradle group with 2 updates ([#16](https://github.com/MarioFronza/siga-bot/issues/16)) ([275b3c7](https://github.com/MarioFronza/siga-bot/commit/275b3c7c5215c7be360df67012deaef511994ddd))

## 1.0.0 (2026-11-20)


### Features

* add container healthcheck on /health ([e1a002c](https://github.com/MarioFronza/siga-bot/commit/e1a002cd28e4fc6c5012f210b891ab0e7b55aedd))
* fail fast naming missing environment variables ([997c072](https://github.com/MarioFronza/siga-bot/commit/997c072c876536aad8d7e45b6770843bef9d7443))
