# KStatus

This is a demo project that aims to implement functionality 
of tracking availability of given URLs over time.
The main goal of this project is to try the technologies mentioned below, 
while targeting deployment to AWS.

### Tech Stack

* Ktor
* Kodein
* Arrow
* jasync (to access MySQL)
* lettuce (to access Redis)
* kotest
* Docker

### Dependencies

* MySQL (AWS Aurora)
* Redis (AWS ElastiCache)
* SQS

### TODOs

- [ ] Gradle multi-module project
- [ ] Set-up Ktor
- [ ] Set-up Kodein
- [ ] Set-up Arrow
- [ ] docker-compose with dependencies
- [ ] terraform for localstack resources
- [ ] Define openapi.yaml
- [ ] At least one test using kotest
- [ ] At least one test using mockk
- [ ] At least one test for db layer
- [ ] At least one test for API (using mock server)
- [ ] At least one integration test for API
- [ ] At least one integration test for the scheduler (writing to SQS)
- [ ] At least one integration test for the worker (reading from SQS)