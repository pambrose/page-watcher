default: versioncheck

clean:
	./gradlew clean

compile:
	./gradlew build -xtest

build: compile

uberjar:
	./gradlew uberjar

uber: uberjar
	java -jar build/libs/server.jar

run:
	./gradlew run

tests:
	./gradlew check

lint:
	./gradlew lintKotlinMain
	./gradlew lintKotlinTest

versioncheck:
	./gradlew dependencyUpdates

depends:
	./gradlew dependencies

upgrade-wrapper:
	./gradlew wrapper --gradle-version=7.5.1 --distribution-type=bin