# KStatus

This is a demo project that handles monitoring of the availability of web pages.
The main goal of this project is to try the technologies mentioned below, 
while keeping in mind a future deployment to AWS.

### Tech Stack

* [Ktor](https://github.com/ktorio/ktor) as a web framework
* [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) for working with JSON
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
- [x] docker-compose with dependencies
- [x] terraform for localstack resources
- [x] Makefile (with up, down, run, ci commands)
- [x] openapi.yaml with API endpoints
- [x] Implement endpoints from OpenAPI spec
- [x] Set up database migrations using FlyWay
- [x] Implement repository layer using jasync
- [x] Add error handling to the API
- [x] Implement an mvp for scheduler
- [x] Implement an mvp for worker
- [x] At least one test using kotest
- [x] At least one test using mockk
- [x] At least one test for db layer
- [x] At least one test for API (using mock server)
- [x] At least one test validating API responses against OpenAPI spec
- [ ] At least one integration test for API
- [ ] At least one integration test for the scheduler (writing to SQS)
- [ ] At least one integration test for the worker (reading from SQS)

### Encountered issues & rough edges

- default way to configure Ktor sucks, as it's relying on a conf file in HOCON format
- stack traces are hard to read or missing line numbers
- [jasync ConnectionPool's API is not super Kotlin friendly](https://github.com/jasync-sql/jasync-sql/issues/184)
- [jasync is using JodaTime instead of java.time.*](https://github.com/jasync-sql/jasync-sql/issues/131)
- [swagger-request-validator has no out-of-the-box support for Ktor](https://bitbucket.org/atlassian/swagger-request-validator/issues/297/ktor-support)
- had to add [ApiTestSpec](https://github.com/ilya40umov/kstatus/blob/master/api/src/test/kotlin/me/ilya40umov/kstatus/api/ApiTestSpec.kt) to be able to write boilerplate free API tests

### Useful links

#### Projects using similar tech

- [Rudge/kotlin-ktor-realworld-example-app](https://github.com/Rudge/kotlin-ktor-realworld-example-app)

#### Coroutines

- [Official Coroutines Guide](https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html)
- [Why exception handling with Kotlin Coroutines is so hard and how to successfully master it!](https://www.lukaslechner.com/why-exception-handling-with-kotlin-coroutines-is-so-hard-and-how-to-successfully-master-it/)
- [SQS Consumer Using Kotlin Coroutines](https://jivimberg.io/blog/2019/02/23/sqs-consumer-using-kotlin-coroutines/)