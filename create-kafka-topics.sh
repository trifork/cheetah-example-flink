#!/bin/bash

# Script to create all Kafka topics required for Flink example jobs
# This script uses the kafka-setup container from cheetah-development-infrastructure
# to create topics in the local Kafka instance

set -e

# Check if CHEETAH_INFRA_PATH is set, otherwise try to find it
if [ -z "$CHEETAH_INFRA_PATH" ]; then
    # Try common locations
    if [ -d "../cheetah-development-infrastructure" ]; then
        CHEETAH_INFRA_PATH="../cheetah-development-infrastructure"
    elif [ -d "../../cheetah-development-infrastructure" ]; then
        CHEETAH_INFRA_PATH="../../cheetah-development-infrastructure"
    elif [ -d "$HOME/dev/cheetah-development-infrastructure" ]; then
        CHEETAH_INFRA_PATH="$HOME/dev/cheetah-development-infrastructure"
    else
        echo "Error: Could not find cheetah-development-infrastructure directory."
        echo "Please set CHEETAH_INFRA_PATH environment variable to point to it."
        echo "Example: export CHEETAH_INFRA_PATH=../cheetah-development-infrastructure"
        exit 1
    fi
fi

echo "Using cheetah-development-infrastructure at: $CHEETAH_INFRA_PATH"
echo ""
echo "Creating Kafka topics..."
echo ""

# All topics from the component tests
TOPICS=(
    # AvroToJson
    "AvroToJsonInputTopic"
    "AvroToJsonOutputTopic"
    
    # DockerComposeExamples/RunMultipleFlinkJobs
    "JsonToAvroInputTopic"
    "JsonToAvroOutputTopic"
    
    # EnrichStream
    "EnrichStreamEnrichTopic"
    "EnrichStreamInputTopic"
    "EnrichStreamOutputTopic"
    
    # ExternalLookup
    "ExternalLookupInputTopic"
    "ExternalLookupOutputTopic"
    
    # FlinkStates
    "FlinkStatesInputTopic"
    "FlinkStatesOutputTopic-value"
    "FlinkStatesOutputTopic-reducing"
    "FlinkStatesOutputTopic-aggregating"
    "FlinkStatesOutputTopic-list"
    "FlinkStatesOutputTopic-map"
    
    # JsonToAvro
    "jsonToAvroInputTopic"
    "jsonToAvroOutputTopic"
    
    # KeySerializationSchema
    "KeySerializationSchemaInputTopic"
    "KeySerializationSchemaOutputTopic"
    
    # MultipleSideOutput
    "MultipleSideOutputExampleInputTopic"
    "OutputA-events"
    "OutputB-events"
    "OutputCD-events"
    
    # Observability
    "ObservabilityInputTopic"
    
    # SerializationErrorCatch
    "SerializationErrorCatchInputTopic"
    "SerializationErrorCatchOutputTopic"
    
    # SerializationErrorSideOutput
    "SerializationErrorSideOutputInputTopic"
    "SerializationErrorSideOutputOutputTopic"
    "SerializationErrorSideOutputOutputTopicUnParsed"
    
    # TransformAndStore
    "TransformAndStoreInputTopic"
    
    # TumblingWindow
    "TumblingWindowInputTopic"
    "TumblingWindowOutputTopic"
)

# Convert array to space-separated string
ALL_TOPICS="${TOPICS[*]}"

echo "Topics to create:"
for topic in "${TOPICS[@]}"; do
    echo "  - $topic"
done
echo ""

# Use the kafka-setup container to create topics
echo "Running kafka-setup container to create topics..."
cd "$CHEETAH_INFRA_PATH"
INITIAL_KAFKA_TOPICS="$ALL_TOPICS" docker compose up kafka-setup -d

echo ""
echo "Waiting for topics to be created..."
sleep 5

echo ""
echo "✓ All topics should now be created!"
echo ""
echo "You can verify the topics were created by visiting Redpanda UI at http://localhost:9898"
