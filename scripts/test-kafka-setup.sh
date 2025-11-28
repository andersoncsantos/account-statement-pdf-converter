#!/bin/bash

# End-to-end test script for Kafka setup
# This script orchestrates the complete test workflow

set -e  # Exit on any error

echo "=========================================="
echo "Kafka Setup End-to-End Test"
echo "=========================================="
echo ""

# Step 1: Start Docker Compose
echo "Step 1: Starting Docker Compose environment..."
docker-compose up -d

if [ $? -eq 0 ]; then
  echo "✓ Docker Compose started successfully"
else
  echo "✗ Failed to start Docker Compose"
  exit 1
fi

echo ""

# Step 2: Wait for Kafka to be ready
echo "Step 2: Waiting for Kafka to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1
  
  if [ $? -eq 0 ]; then
    echo "✓ Kafka is ready"
    break
  fi
  
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "Waiting for Kafka... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
  echo "✗ Kafka failed to start within timeout"
  docker logs kafka
  exit 1
fi

echo ""

# Step 3: Create topic
echo "Step 3: Creating test topic..."
./scripts/create-topic.sh test-topic 1 1

if [ $? -eq 0 ]; then
  echo "✓ Topic creation successful"
else
  echo "✗ Topic creation failed"
  exit 1
fi

echo ""

# Step 4: Publish test message
echo "Step 4: Publishing test message..."
TEST_MESSAGE="End-to-end test message at $(date)"
./scripts/produce-message.sh test-topic "$TEST_MESSAGE"

if [ $? -eq 0 ]; then
  echo "✓ Message published successfully"
else
  echo "✗ Message publishing failed"
  exit 1
fi

echo ""

# Step 5: Consume and verify message
echo "Step 5: Consuming and verifying message..."
CONSUMED_MESSAGE=$(docker exec kafka kafka-console-consumer \
  --topic test-topic \
  --from-beginning \
  --max-messages 1 \
  --timeout-ms 10000 \
  --bootstrap-server localhost:9092 2>/dev/null)

if [ -n "$CONSUMED_MESSAGE" ]; then
  echo "✓ Message consumed successfully"
  echo "  Consumed: $CONSUMED_MESSAGE"
else
  echo "✗ Failed to consume message"
  exit 1
fi

echo ""
echo "=========================================="
echo "✓ All tests passed successfully!"
echo "=========================================="
echo ""
echo "Your Kafka setup is working correctly."
echo "You can now use the following commands:"
echo "  - Create topic: ./scripts/create-topic.sh [topic-name]"
echo "  - Produce message: ./scripts/produce-message.sh [topic-name] [message]"
echo "  - Consume messages: ./scripts/consume-messages.sh [topic-name]"
echo ""
echo "To stop the environment: docker-compose down"
