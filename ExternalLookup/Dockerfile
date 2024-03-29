#
#  Install dependencies, compile sources, build jars.
#
FROM maven:3.9.6-eclipse-temurin-17-alpine as build

WORKDIR /app

ARG MAVEN_ARGS

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
    mvn dependency:resolve dependency:resolve-plugins -B -U -s ./settings.xml -Dmaven.repo.local="${CI_PROJECT_DIR-.}"/.m2/repository

# Then add source.
COPY src ./src

# Finally compile, test, and jar up
RUN --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=./.m2,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn package -B -s ./settings.xml ${MAVEN_ARGS-} -Dmaven.repo.local="${CI_PROJECT_DIR-.}"/.m2/repository

#
#  Install jars onto clean runtime system.
#
FROM flink:1.18.1-scala_2.12-java17 as runtime

COPY --from=build /app/target/externallookup*.jar /opt/flink/usrlib/artifacts/externallookup.jar

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# Add support for structured JSON logging
RUN log4jVersion=$(find /opt/flink/lib/ -name 'log4j-core-*.jar' -exec basename {} \; | grep -oP '(\d+\.\d+\.\d+)') && \
    wget --progress=dot:giga -P /opt/flink/lib "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-layout-template-json/$log4jVersion/log4j-layout-template-json-$log4jVersion.jar"

RUN mkdir /opt/flink/plugins/s3-fs-hadoop/; \
    mkdir /opt/flink/plugins/s3-fs-presto/; \
    mkdir ./plugins/azure-fs-hadoop; \
    cp /opt/flink/opt/flink-s3-fs-hadoop-1.18.1.jar /opt/flink/plugins/s3-fs-hadoop/; \
    cp /opt/flink/opt/flink-s3-fs-presto-1.18.1.jar /opt/flink/plugins/s3-fs-presto/; \
    cp ./opt/flink-azure-fs-hadoop-1.18.1.jar ./plugins/azure-fs-hadoop/

RUN mkdir -p /checkpoints && chmod 777 /checkpoints

USER flink
