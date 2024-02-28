#
#  Install dependencies, compile sources, build jars.
#
FROM maven:3.9.6-eclipse-temurin-17-alpine as build

WORKDIR /app

COPY pom.xml ./
COPY settings.xml ./
# Copy in local repository
COPY .m2/ /root/.m2/

# Download dependencies (with docker cache)
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=./.m2,sharing=locked \
    cp -rn /root/.m2/ ./ &&\
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn dependency:go-offline -B -U -s ./settings.xml

# Then add source.
COPY src ./src

# Finally compile, test, and jar up
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=./.m2,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn package -B -s ./settings.xml ${MAVEN_ARGS-}

#
#  Install jars onto clean runtime system.
#
FROM ghcr.io/trifork/cheetah-lib-processing:E2E-SNAPSHOT1508 as runtime

COPY --from=build /app/target/tumblingwindow*.jar /opt/flink/usrlib/artifacts/

USER flink
