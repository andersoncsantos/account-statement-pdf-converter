#!/bin/bash

# Script to produce an Avro TransactionAvro message to a Kafka topic
# Usage: ./scripts/produce-message.sh [date] [description] [amount] [balance]

DATE=${1:-$(date +%Y-%m-%d)}
DESCRIPTION=${2:-"PIX RECEBIDO"}
AMOUNT=${3:-"150.00"}
BALANCE=${4:-null}

echo "Publishing TransactionAvro to transactions-topic"
echo "Date: $DATE | Description: $DESCRIPTION | Amount: $AMOUNT | Balance: $BALANCE"

if [ "$BALANCE" = "null" ]; then
  JSON_MESSAGE='{"date":"'"$DATE"'","description":"'"$DESCRIPTION"'","amount":"'"$AMOUNT"'","balance":null}'
else
  JSON_MESSAGE='{"date":"'"$DATE"'","description":"'"$DESCRIPTION"'","amount":"'"$AMOUNT"'","balance":{"string":"'"$BALANCE"'"}}'
fi

echo "$JSON_MESSAGE" | docker exec -i schema-registry kafka-avro-console-producer \
  --topic transaction-events \
  --bootstrap-server kafka:29092 \
  --property schema.registry.url=http://schema-registry:8081 \
  --property value.schema='{"type":"record","name":"TransactionAvro","namespace":"com.anderson.pdf_converter.avro","doc":"Represents a financial transaction extracted from a PDF statement","fields":[{"name":"date","type":"string","doc":"Transaction date in ISO-8601 format (YYYY-MM-DD)"},{"name":"description","type":"string","doc":"Transaction description or merchant name"},{"name":"amount","type":"string","doc":"Transaction amount as a string to preserve formatting"},{"name":"balance","type":["null","string"],"default":null,"doc":"Account balance after transaction (optional)"}]}'

if [ $? -eq 0 ]; then
  echo "✓ TransactionAvro published successfully"
else
  echo "✗ Failed to publish TransactionAvro"
  exit 1
fi
