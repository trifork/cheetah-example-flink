#
#  Install dependencies, compile sources, build jar.
#
FROM maven:3.9.6-eclipse-temurin-17-alpine as build

WORKDIR /app

COPY pom.xml ./
COPY settings.xml ./
COPY .mvn .mvn

# Download dependencies (with docker cache)
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=/root/.m2,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn dependency:go-offline -B

# Then add source.
COPY src ./src

# Finally compile, test, and jar up
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=/root/.m2,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn package -B

#
#  Install jar onto clean runtime system.
#
FROM ghcr.io/trifork/cheetah-lib-processing:4.2.0-feature-lib-processing-image-snapshot36-flink-1.18.1-java11

COPY --from=build /app/target/jsonToAvro*.jar /opt/flink/usrlib/artifacts/jsonToAvro.jar

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# Add support for structured JSON logging
RUN log4jVersion=$(find /opt/flink/lib/ -name 'log4j-core-*.jar' -exec basename {} \; | grep -oP '(\d+\.\d+\.\d+)') && \
    wget --progress=dot:giga -P /opt/flink/lib "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-layout-template-json/$log4jVersion/log4j-layout-template-json-$log4jVersion.jar"

USER flink
