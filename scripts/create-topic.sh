#!/bin/bash

# Script to create a Kafka topic
# Usage: ./scripts/create-topic.sh [topic-name] [partitions] [replication-factor]

TOPIC_NAME=${1:-test-topic}
PARTITIONS=${2:-1}
REPLICATION_FACTOR=${3:-1}

echo "Creating Kafka topic: $TOPIC_NAME with $PARTITIONS partition(s) and replication factor $REPLICATION_FACTOR"

docker exec kafka kafka-topics --create \
  --topic "$TOPIC_NAME" \
  --partitions "$PARTITIONS" \
  --replication-factor "$REPLICATION_FACTOR" \
  --if-not-exists \
  --bootstrap-server localhost:9092

if [ $? -eq 0 ]; then
  echo "✓ Topic '$TOPIC_NAME' created successfully or already exists"
  
  # Verify topic exists
  echo "Verifying topic exists..."
  docker exec kafka kafka-topics --list --bootstrap-server localhost:9092 | grep -q "^$TOPIC_NAME$"
  
  if [ $? -eq 0 ]; then
    echo "✓ Topic '$TOPIC_NAME' verified in broker"
  else
    echo "✗ Warning: Could not verify topic in broker"
  fi
else
  echo "✗ Failed to create topic '$TOPIC_NAME'"
  exit 1
fi
