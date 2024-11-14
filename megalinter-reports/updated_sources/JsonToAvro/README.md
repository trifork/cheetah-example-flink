# jsonToAvro

This repository contains a templated flink job. Processing is handled by Apache Flink which is a statefull scalable stream processing framework. You can find more information about Apache Flink [here](https://flink.apache.org/).

The flink job consumes messages from a kafka topic with simple Avro serialized messages, enriches these messages and publishes the enriched Avro serialized  messages on another kafka topic.

## Project structure

The following list explains some of the major components of the project, in order to familiarize newcomers with the structure:

- `/src` - Java source code containing the Flink job
  - `main` - Source code for the job itself
  - `test` - Unit tests for the job
- `/ComponentTest` - .NET test project, which is used for component testing the Flink job
  - `Dockerfile` - to allow testing within Docker
- `Dockerfile` - for building the Flink job.
- `docker-compose.yaml` - allows running the Flink job and component test within Docker, with necessary environment values, port bindings, etc. for local development.
- `pom.xml` - Project Object Model, which defines how to build the Flink job, which dependencies it requires and necessary plugins for building and testing.
- `README.md` - This file
- `settings.xml` - Settings file used to configure access to the Maven Package Repository.

## Prerequisites

This project features a Flink job developed using Java 11 alongside a component test in .NET6
For a seamless experience building and running the project, it's essential to have both Java 11 and the .NET 6 SDK installed.

This project depends on maven packages that are located on the
Cheetah Maven Repository on github,
and thus requires access to the Cheetah Maven Repository on Github.
To enable access you need to get create a personal token by going to
[https://github.com/settings/tokens/new](https://github.com/settings/tokens/new)
and creating a token with the `read:packages` scope.

### Run docker containers

To run the docker containers you need to set the following environment variables:

```bash
export GITHUB_ACTOR=your-github-username # replace with your github username
export GITHUB_TOKEN=your-github-token # replace with the token you generated in the previous paragraph
```

Then verify that you're able to build docker images by running:

```bash
docker compose build
```

## Avro serialization

This project utilizes Avro serialization for message exchange with Kafka. Avro depends on schemas for message serialization. More information about this can be found in the official Avro documentation (`https://avro.apache.org/docs/`).

Schema Location:
The Avro schemas used in this project are located at /src/main/avro.

Java & .NET Specialized Classes:
Both Java and .NET require specialized classes derived from ISpecificRecord to represent Avro serialized messages for production and consumption. Thankfully, these classes can be automatically generated using Apache Avro tools.
The Java classes in this project is used in the actual job, and the .NET classes are used in the componenttest.

For Java:
To generate classes, execute the following at the project root:

```bash
mvn compile
```

This action produces Java classes based on the schemas found in `/src/main/avro` and stores them in `/src/main/java/model`.
The source and target directories for Avro schemas and the Java classes they generate can be adjusted in the pom.xml file, specifically within the plugin section having the groupId `org.apache.avro`.

For .NET:
The generation of specialized Avro classes for .NET, crucial for component testing, is slightly more tedious. To simplify the process, these classes have been pre-generated in this project, ensuring the component-test functions seamlessly.
However, should you modify the Avro schema, follow the steps below. It's vital to use identical schemas when generating both Java and .NET classes to prevent discrepancies.

First install nuget package `Apache.Avro.Tools` with the following command:

```bash
dotnet tool install --global Apache.Avro.Tools --version 1.11.2
```

Then run the following command to generate the .NET classes

```bash
avrogen -s <path-to-avro-schema> <target-location>
```

Replace <path-to-avro-schema> with the path to the raw Avro schemas, and <target-location> with the desired directory for the .NET classes you're generating.

## Local development

For local development, you will need to clone the [cheetah-development-infrastructure](https://github.com/trifork/cheetah-development-infrastructure) repository.

You'll then be able to run necessary infrastructure with the following command from within that repository:

```bash
docker compose --profile kafka up -d
```

This will start `kafka`, `keycloak` , `schema-registry` (used for AVRO schemas) and `redpanda`, which can be used to inspect what topics and messages exist in `kafka`.

Redpanda can be accessed on [http://localhost:9898](http://localhost:9898).

You need to create the source topics which the flink job reads from. This is done by setting the `INITIAL_KAFKA_TOPICS` to `jsonToAvroInputTopic jsonToAvroOutputTopic` before running the `kafka-setup` container in `cheetah-development-infrastructure`:
```powershell
$env:INITIAL_KAFKA_TOPICS="jsonToAvroInputTopic jsonToAvroOutputTopic"; docker compose up kafka-setup -d
```
The `kafka-setup` service is also run when starting kafka using the above command, but running it seperately enables you to create the topics with an already running Kafka.
Alternatively you can create the topics manually in Redpanda.

`cheetah-development-infrastructure` contains more than just the services that the above command starts, but running the entire infrastructure setup takes up a fair amount of resources on your local system.

If you need to run other services like OpenSearch, please see the documentation in the `development-infrastructure` repository.

## Inspect and modify using Intellij

When developing your job you can run/debug it like any other Java application by running the `main` method in jsonToAvroJob.

1. Configure the project to use JDK 11: File > Project Structure: Project > Project SDK: 11

1. Verify that the project builds by pressing `Ctrl + F9`
    > [!WARNING]
    > If either the project does not build or your Intellij is showing errors in the file, try syncing your maven projects by unfolding the Maven window (by default the top tab in the small vertical bar on the right of the IDE) and pressing the "Reload All Maven Projects"-button in the top-left of that window, then, restart Intellij.
    > The very first build can take a while, as maven builds up its cache.

1. Navigate to the file `jsonToAvroJob.java`, under the `src/main/java/cheetah/example/job/` folder.
1. This file contains a job that performs a mapping on all incoming `InputEvents`. It consists of the following pieces:
    <details>
    <!-- Have an empty line after the <details> tag or markdown blocks will not render. -->

    - A static main method, which is used to start the job itself.
    - A setup method, which is where the main functionality resides. It sets up:
      - A source - Determines which input source to use, in this case Kafka, and how to connect to it.
      - An input stream - Specifies the datatype that is ingested from the source, which in this case is `InputEvent`
      - An output stream - This is where we transform the incoming data - in this case we apply a basic mapping from `InputEvent` to `OutputEvent`. It also determines the output datatype, which in this case is `OutputEvent`
      - A sink - Determines the destination to output the results of our job to, in this case Kafka
      - A single call that connects the output stream to the sink
    </details>

1. Create Intellij run profile for jsonToAvroJob.java with parameters, by right-clicking the file `jsonToAvroJob` and select `more` followed by `Modify Run Configuration...`.
1. Enter the following program arguments:

  ```
  --kafka-bootstrap-servers localhost:9092
  --input-kafka-topic jsonToAvroInputTopic
  --output-kafka-topic jsonToAvroOutputTopic
  --sr-url http://localhost:8081/apis/ccompat/v7
  --kafka-group-id jsonToAvro-group-id
  ```

  And add the following Environment variables:

  ```
  - KAFKA_CLIENT_ID=default-access
  - KAFKA_CLIENT_SECRET=default-access-secret
  - KAFKA_SCOPE=kafka
  - KAFKA_TOKEN_URL=http://localhost:1852/realms/local-development/protocol/openid-connect/token
  - SCHEMA_REGISTRY_CLIENT_ID=default-access
  - SCHEMA_REGISTRY_CLIENT_SECRET=default-access-secret
  - SCHEMA_REGISTRY_SCOPE=schema-registry
  - SR_TOKEN_URL=http://localhost:1852/realms/local-development/protocol/openid-connect/token
  ```
  You can insert the following string in the `Environment variables` field:

  ```text
  KAFKA_CLIENT_ID=flink;KAFKA_CLIENT_SECRET=testsecret;KAFKA_TOKEN_URL=http://localhost:1752/oauth2/token;KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
  ```
1. Notice how the job is configured to consume events from topic `jsonToAvroInputTopic` and output to `jsonToAvroOutputTopic`
1. Save configuration by clicking OK
  > [!IMPORTANT]
  > When running the job you might see a warning in the console informing that *An illegal reflective access operation has occurred*, which can be ignored.

## Tests
### Unit tests

This project contains a sample Unit test in `src/test/java/cheetah/example/job/jsonToAvroMapperTest.java`, which utilizes JUnit5.

Unit tests are automatically run as part of the build processing when building the Flink job through Docker.

### Component test

This project contains a .NET test project under `/ComponentTest`, which you can use to verify that your job acts as you expect it to.

You can run both these tests from your preferred IDE using the `.sln` file and through docker.

In order to run both your job and the component tests at once, simply run `docker compose up --build` from the root directory of the repository.

Alternatively, if you just want to run your job from docker compose and run the component tests manually through your IDE, run the following command:

```sh
docker compose up jsontoavro-jobmanager jsontoavro-taskmanager --build
```

Similarly, you can run just the component test through docker compose using:
```sh
docker compose up jsontoavro-test --build
```

If doing so, make sure to run your job locally from Intellij before starting the component test.

The component test is producing a single message to `jsonToAvroInputTopic`, and listening for any messages published to `jsonToAvroOutputTopic`. It expects the flink job to publish the same message, enriched with a new field, on `jsonToAvroOutputTopic`.
You can observe the topics, produced messages and schemas at [http://localhost:9898](http://localhost:9898).

#### Persisted data during development

You might encounter failing component tests due to your job receiving more messages than expected. This occurs if you've previously run the job and component tests and data is still present in Kafka. The component test will then, in some cases, re-read the output of previous runs.

To fix this, you'll need to delete the data in Kafka by running: `docker compose down` in the `cheetah-development-infrastructure` repository and then starting it again using the command in [Local development with docker-compose](#local-development). This deletes the volume containing Kafka's data and starts everything up again.

#### Concurrent tests

While .NET allows running multiple tests in parallel, it is generally a difficult task to ensure that a Flink job correctly handles receiving data intended for testing multiple different "scenarios" at once.

This is a fairly complicated topic to explain accurately, which involves both the way Flink handles incoming messages and how time progresses from the perspective of a Flink Job. The Flink documentation is generally good at explaining many of these nuances - A solid place to start would be: [Timely Stream Processing | Apache Flink](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/concepts/time/).

The general recommendation is to avoid running multiple component tests at the same time. You *may* be able to run multiple tests in succession, but, depending on the implementation and requirements of both the job and the test, your mileage may wary.

Because of this component testing should, in most scenarios, be kept to testing the job generally (primary functionality, correct input/output models, etc.) while ensuring the finer details of the implementation with unit tests.

## Implementing a new Flink job

### What is a job?

A job in Flink consists of a number of sources, a number of sinks, and a number of transformation steps.

From each source you will get a stream. This stream can be transformed, split, or combined with other streams. Each transformation will take a stream as input and return a stream that can be used as an input for another transformation or sent to a sink.

Some process functions (transformation) can combine two sources. By specifying a common property in `keyBy` it is ensured that related events from the two sources will run in the same thread and have access to the same state. An example could be data enrichment where a main stream is combined with a secondary stream, returning a new stream enriched by data from the secondary stream.

Besides the main stream returned from a process function, some type of functions can generate side outputs which will generate secondary streams that can be consumed by other transformations or sinks. This allows for functions to generate multiple events from a single event.

![example of dataflow](./images/program_dataflow.svg)

### KISS

The job should have minimum responsibility and business logic. Better to have multiple, simple jobs with a small amount of responsibility, than a single, complex job.

In most scenarios, output data should be stored in a Kafka topic. This makes it possible for others to consume the data your job outputs.

If data needs to be persisted in a database for direct query by some other service, this should be done by the standard storage job.

### TDD

An approach proven to be good, is to start by writing a unit test for the processing job under development.
Then proceed in micro iterations, carefully assert expected output and behavior.
The project includes several examples for JUnit tests.

It is not recommended to use Mockto, since Flink is not happy about it and will produce unstable results.