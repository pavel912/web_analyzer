DEFAULT_GOAL := build-run

clean:
	./gradlew clean

build:
	 ./gradlew clean build test

run:
	./gradlew run

test:
	./gradlew test

report:
	./gradlew test jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

update-deps:
	./gradlew useLatestVersions

stage:
	./gradlew stage

build-run: build run

.PHONY: build