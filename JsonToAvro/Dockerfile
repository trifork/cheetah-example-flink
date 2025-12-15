#
#  Install dependencies, compile sources, build jar.
#
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

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
FROM ghcr.io/trifork/cheetah-flink:6.2.0-RC.6.2.1-flink-1.20.3-java11

COPY --from=build /app/target/jsonToAvro*.jar /opt/flink/usrlib/artifacts/jsonToAvro.jar

USER flink
