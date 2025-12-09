#!/bin/bash

# Script to consume messages from a Kafka topic
# Usage: ./scripts/consume-messages.sh [topic-name]

TOPIC_NAME=${1:-test-topic}

echo "Consuming messages from topic: $TOPIC_NAME"
echo "Reading from beginning of topic..."
echo "Press Ctrl+C to stop"
echo "----------------------------------------"

docker exec kafka kafka-console-consumer \
  --topic "$TOPIC_NAME" \
  --from-beginning \
  --bootstrap-server kafka:9092

if [ $? -ne 0 ]; then
  echo "✗ Failed to consume messages from topic '$TOPIC_NAME'"
  echo "Make sure the topic exists and Kafka is running"
  exit 1
fi
