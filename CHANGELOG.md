# Changelog

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
