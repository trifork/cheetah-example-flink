#
#  Install dependencies, compile sources, build jar.
#
FROM maven:3.9.7-eclipse-temurin-17-alpine as build

WORKDIR /app

COPY pom.xml ./
COPY settings.xml ./
COPY .mvn .mvn

# Download dependencies (with docker cache)
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=/root/.m2/repository,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn dependency:go-offline -B

# Then add source.
COPY src ./src

# Finally compile, test, and jar up
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=/root/.m2/repository,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn package -B

#
#  Install jar onto clean runtime system.
#
FROM ghcr.io/trifork/cheetah-flink:5.2.0-bump-flink-version-SNAPSHOT78-flink-1.19.1-java17

COPY --from=build /app/target/jsonToAvro*.jar /opt/flink/usrlib/artifacts/jsonToAvro.jar

USER flink
