# KStatus

This is a demo project that aims to implement functionality 
of tracking availability of given URLs over time.
The main goal of this project is to try the technologies mentioned below, 
while keeping in mind a future deployment to AWS.

### Tech Stack

* [Ktor](https://github.com/ktorio/ktor) as a web framework
* [Kodein](https://github.com/Kodein-Framework/Kodein-DI) for DI
* [Hoplite](https://github.com/sksamuel/hoplite) to load configuration files
* [jasync-sql](https://github.com/jasync-sql/jasync-sql) to access MySQL
* [kotest](https://github.com/kotest/kotest) for tests
* [mockk](https://github.com/mockk/mockk) for mocks

### Dependencies

* MySQL (AWS Aurora)
* SQS

### TODOs

- [x] Gradle multi-module project
- [x] Set-up Ktor
- [x] Set-up Kodein
- [x] Set up Micrometer & Prometheus
- [x] Configuration via YAML
- [ ] docker-compose with dependencies
- [ ] terraform for localstack resources
- [ ] Makefile (with up, down, run, ci commands)
- [ ] openapi.yaml with API endpoints
- [ ] Implement endpoints from OpenAPI spec
- [ ] At least one test using kotest
- [ ] At least one test using mockk
- [ ] At least one test for db layer
- [ ] At least one test for API (using mock server)
- [ ] At least one integration test for API
- [ ] At least one integration test for the scheduler (writing to SQS)
- [ ] At least one integration test for the worker (reading from SQS)

### Useful links

#### Projects using similar tech

- [Rudge/kotlin-ktor-realworld-example-app](https://github.com/Rudge/kotlin-ktor-realworld-example-app)

#### Coroutines

- [Official Coroutines Guide](https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html)
- [Why exception handling with Kotlin Coroutines is so hard and how to successfully master it!](https://www.lukaslechner.com/why-exception-handling-with-kotlin-coroutines-is-so-hard-and-how-to-successfully-master-it/)