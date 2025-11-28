#!/bin/bash

# Script to produce a message to a Kafka topic
# Usage: ./scripts/produce-message.sh [topic-name] [message]

TOPIC_NAME=${1:-test-topic}
MESSAGE=${2:-"Hello Kafka! This is a test message."}

echo "Publishing message to topic: $TOPIC_NAME"
echo "Message: $MESSAGE"

echo "$MESSAGE" | docker exec -i kafka kafka-console-producer \
  --topic "$TOPIC_NAME" \
  --bootstrap-server localhost:9092

if [ $? -eq 0 ]; then
  echo "✓ Message published successfully to topic '$TOPIC_NAME'"
else
  echo "✗ Failed to publish message to topic '$TOPIC_NAME'"
  echo "Make sure the topic exists and Kafka is running"
  exit 1
fi
