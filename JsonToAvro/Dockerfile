#
#  Install dependencies, compile sources, build jars.
#
FROM maven:3.8.6-openjdk-11 as build

WORKDIR /app

ARG MAVEN_ARGS

COPY pom.xml ./
COPY settings.xml ./
# Copy in local repository
COPY .m2/ /root/.m2/

# Download all dependencies, excluding the ones marked as 'provided';
# Change scope of Flink dependencies from 'compile' to 'provided'.
# This basically instructs Maven to not download the jar files,
# but rely on them being provided by other means,
# and effectively results in a fat har without Flink jars.
# This is because the runtime docker container provides the Flink jars
# itself and adds them on the classpath for the fat jar to resolve.
RUN sed -i pom.xml -e 's,<scope>compile</scope>,<scope>provided</scope>,'

# Download dependencies (with docker cache)
RUN \
    --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=./.m2,sharing=locked \
    cp -rn /root/.m2/ ./ &&\
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn dependency:resolve dependency:resolve-plugins -B -U -s ./settings.xml -Dmaven.repo.local="${CI_PROJECT_DIR-.}"/.m2/repository

# Then add source.
COPY src ./src

# Finally compile, test, and jar up
RUN \
    --mount=type=secret,id=GITHUB_TOKEN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=cache,target=./.m2,sharing=locked \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    mvn package -B -s ./settings.xml ${MAVEN_ARGS-} -Dmaven.repo.local="${CI_PROJECT_DIR-.}"/.m2/repository

#
#  Install jars onto clean runtime system.
#
FROM flink:1.16.1-scala_2.12-java11 as runtime

COPY --from=build /app/target/lib/*.jar /opt/flink/usrlib/artifacts/
COPY --from=build /app/target/jsonToAvro*.jar /opt/flink/usrlib/artifacts/

RUN mkdir /opt/flink/plugins/s3-fs-hadoop/; \
    mkdir /opt/flink/plugins/s3-fs-presto/; \
    mkdir ./plugins/azure-fs-hadoop; \
    cp /opt/flink/opt/flink-s3-fs-hadoop-1.16.1.jar /opt/flink/plugins/s3-fs-hadoop/; \
    cp /opt/flink/opt/flink-s3-fs-presto-1.16.1.jar /opt/flink/plugins/s3-fs-presto/; \
    cp ./opt/flink-azure-fs-hadoop-1.16.1.jar ./plugins/azure-fs-hadoop/

RUN mkdir -p /checkpoints && chmod 777 /checkpoints

USER flink
