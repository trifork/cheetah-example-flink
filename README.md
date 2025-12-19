# Cheetah Platform - Apache Flink Examples

A collection of example Apache Flink streaming jobs demonstrating various data processing patterns and best practices for the Cheetah data platform.

## Overview

This repository contains multiple real-world examples of Apache Flink jobs, each showcasing different streaming data processing concepts:

- **[AvroToJson](AvroToJson/)** - Convert Avro-serialized messages to JSON format
- **[EnrichStream](EnrichStream/)** - Enrich streaming data with additional information
- **[ExternalLookup](ExternalLookup/)** - Perform lookups against external APIs during stream processing
- **[FlinkStates](FlinkStates/)** - Demonstrate Flink's stateful processing capabilities (ValueState, ReducingState, AggregatingState, ListState, MapState)
- **[JsonToAvro](JsonToAvro/)** - Convert JSON messages to Avro format with schema registry integration
- **[KeySerializationSchema](KeySerializationSchema/)** - Custom serialization for Kafka message keys
- **[MultipleSideOutput](MultipleSideOutput/)** - Split streams into multiple outputs based on conditions
- **[Observability](Observability/)** - Monitor and instrument Flink jobs with metrics and logging
- **[SerializationErrorCatch](SerializationErrorCatch/)** - Handle serialization errors gracefully
- **[SerializationErrorSideOutput](SerializationErrorSideOutput/)** - Route serialization errors to a separate output stream
- **[TransformAndStore](TransformAndStore/)** - Transform data and store results in multiple sinks
- **[TumblingWindow](TumblingWindow/)** - Implement time-based windowing operations

## Prerequisites

### Local Infrastructure

All examples require the local development infrastructure from the [cheetah-development-infrastructure](https://github.com/trifork/cheetah-development-infrastructure) repository. This provides:

- Apache Kafka for message streaming
- Schema Registry for Avro schemas
- Keycloak for authentication
- Redpanda Console for Kafka inspection

Start the infrastructure:

```bash
# Clone the infrastructure repo
git clone https://github.com/trifork/cheetah-development-infrastructure
cd cheetah-development-infrastructure

# Start Kafka and related services
docker compose --profile kafka up -d
```

### GitHub Package Authentication

The Flink jobs depend on Maven packages from the Cheetah Maven Repository on GitHub. You'll need to:

1. Create a GitHub Personal Access Token with `read:packages` scope at [https://github.com/settings/tokens/new](https://github.com/settings/tokens/new)
2. Set environment variables:

```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-github-token
```

### Kafka Topics

Each example requires **specific Kafka topics** to be created before running. Refer to each job's **README** for the required topics and setup instructions.

## Getting Started

Each example is self-contained with:

- **`/src`** - Java source code for the Flink job
- **`/ComponentTest`** - .NET integration tests
- **`docker-compose.yaml`** - Configuration for running locally
- **`README.md`** - Detailed documentation and setup instructions

To run an example:

```bash
# Navigate to an example directory
cd AvroToJson

# Build and start the Flink job
docker compose up --build
```

For detailed information about each job, including specific configuration, testing procedures, and implementation details, see the README in each example's directory.

## Testing

Each example includes:

- **Unit tests** - JUnit tests in `/src/test` (automatically run during Maven build)
- **Component tests** - .NET integration tests in `/ComponentTest` that verify end-to-end behavior

Run component tests via Docker Compose or directly with `dotnet test`.