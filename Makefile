PHONY: build clean up down ci
DEFAULT_TARGET: build

build:
	./gradlew clean build -x test

clean:
	./gradlew clean

up:
	./environment/bin/kstatus_env_up

down:
	./environment/bin/kstatus_env_down

ci:
	./environment/bin/kstatus_ci

swagger-ui:
	docker-compose -f docker-compose.yaml -f docker-compose.tasks.yaml up -d swagger-ui