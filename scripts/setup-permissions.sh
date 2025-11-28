#!/bin/bash

# Script to set executable permissions on all Kafka scripts
# Run this once after cloning the repository

echo "Setting executable permissions on Kafka scripts..."

chmod +x scripts/create-topic.sh
chmod +x scripts/produce-message.sh
chmod +x scripts/consume-messages.sh
chmod +x scripts/test-kafka-setup.sh

echo "✓ All scripts are now executable"
echo ""
echo "You can now run:"
echo "  ./scripts/test-kafka-setup.sh    - Run complete end-to-end test"
echo "  ./scripts/create-topic.sh        - Create a Kafka topic"
echo "  ./scripts/produce-message.sh     - Publish a message"
echo "  ./scripts/consume-messages.sh    - Consume messages"
