# Schema Registry Integration Guide

## Overview

This guide explains how to use the Confluent Schema Registry integration in the PDF Converter application. The Schema Registry provides centralized schema management, versioning, and validation for Kafka messages using Apache Avro serialization.

## Table of Contents

- [Getting Started](#getting-started)
- [Starting Schema Registry](#starting-schema-registry)
- [Defining Avro Schemas](#defining-avro-schemas)
- [Schema Registration](#schema-registration)
- [Producing Avro Messages](#producing-avro-messages)
- [Consuming Avro Messages](#consuming-avro-messages)
- [Schema Evolution](#schema-evolution)
- [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

- Docker and Docker Compose installed
- Kafka and Zookeeper running
- Java 17 or higher
- Gradle 8.x

### Architecture

```
Producer → Avro Serializer → Schema Registry → Kafka → Avro Deserializer → Consumer
                    ↓                                           ↑
              Register Schema                            Retrieve Schema
```

## Starting Schema Registry

### Using Docker Compose

The Schema Registry is included in the `docker-compose.yml` file. Start all services:

```bash
docker-compose up -d
```

This starts:
- Zookeeper (port 2181)
- Kafka (port 9092)
- Schema Registry (port 8081)

### Verify Schema Registry is Running

Check the health endpoint:

```bash
curl http://localhost:8081/subjects
```

Expected response: `[]` (empty array if no schemas registered yet)

### Schema Registry Configuration

The Schema Registry is configured in `docker-compose.yml`:

```yaml
schema-registry:
  image: confluentinc/cp-schema-registry:7.5.0
  ports:
    - "8081:8081"
  environment:
    SCHEMA_REGISTRY_HOST_NAME: schema-registry
    SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: kafka:9092
    SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
  depends_on:
    - zookeeper
    - kafka
```

## Defining Avro Schemas

### Schema File Location

Avro schema files are stored in `src/main/avro/` with the `.avsc` extension.

### Schema Structure

Example Transaction schema (`src/main/avro/Transaction.avsc`):

```json
{
  "type": "record",
  "name": "TransactionAvro",
  "namespace": "com.anderson.pdf_converter.avro",
  "doc": "Represents a financial transaction extracted from a PDF statement",
  "fields": [
    {
      "name": "date",
      "type": "string",
      "doc": "Transaction date in ISO-8601 format (YYYY-MM-DD)"
    },
    {
      "name": "description",
      "type": "string",
      "doc": "Transaction description or merchant name"
    },
    {
      "name": "amount",
      "type": "string",
      "doc": "Transaction amount as a string to preserve formatting"
    },
    {
      "name": "balance",
      "type": ["null", "string"],
      "default": null,
      "doc": "Account balance after transaction (optional)"
    }
  ]
}
```

### Avro Data Types

Common Avro primitive types:
- `string` - UTF-8 text
- `int` - 32-bit signed integer
- `long` - 64-bit signed integer
- `float` - Single precision floating point
- `double` - Double precision floating point
- `boolean` - Binary value
- `null` - No value

### Optional Fields

Use union types with `null` for optional fields:

```json
{
  "name": "optionalField",
  "type": ["null", "string"],
  "default": null
}
```

### Code Generation

The Gradle Avro plugin automatically generates Kotlin classes from schema files during build:

```bash
./gradlew build
```

Generated classes appear in `build/generated-main-avro-java/`.

## Schema Registration

### Automatic Registration

Schemas are automatically registered on first use when `auto.register.schemas` is enabled:

```yaml
spring:
  kafka:
    producer:
      properties:
        auto.register.schemas: true
```

When you send your first message, the serializer:
1. Checks if the schema exists in the registry
2. If not, registers it automatically
3. Caches the schema ID for future use

### Subject Naming

Schemas are stored under subjects. The default naming strategy is `TopicNameStrategy`:

```
Subject name = {topic-name}-value
```

Example: For topic `transaction-events`, the subject is `transaction-events-value`.

### Manual Schema Registration

You can manually register schemas using the REST API:

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"TransactionAvro\",\"namespace\":\"com.anderson.pdf_converter.avro\",\"fields\":[{\"name\":\"date\",\"type\":\"string\"}]}"}' \
  http://localhost:8081/subjects/transaction-events-value/versions
```

### Viewing Registered Schemas

List all subjects:

```bash
curl http://localhost:8081/subjects
```

Get latest schema for a subject:

```bash
curl http://localhost:8081/subjects/transaction-events-value/versions/latest
```

Get schema by ID:

```bash
curl http://localhost:8081/schemas/ids/1
```

## Producing Avro Messages

### Producer Configuration

Configure the Kafka producer in `application.yaml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      properties:
        schema.registry.url: http://localhost:8081
        auto.register.schemas: true
        value.subject.name.strategy: io.confluent.kafka.serializers.subject.TopicNameStrategy
```

### Producer Code Example

```kotlin
@Service
class KafkaMessageProducer(
    private val kafkaTemplate: KafkaTemplate<String, TransactionAvro>
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun sendTransaction(topic: String, transaction: TransactionAvro) {
        try {
            kafkaTemplate.send(topic, transaction).get()
            logger.info("Sent transaction to topic: {}", topic)
        } catch (e: SerializationException) {
            logger.error("Failed to serialize transaction", e)
            throw e
        }
    }
    
    fun sendTransaction(topic: String, key: String, transaction: TransactionAvro) {
        try {
            kafkaTemplate.send(topic, key, transaction).get()
            logger.info("Sent transaction with key {} to topic: {}", key, topic)
        } catch (e: SerializationException) {
            logger.error("Failed to serialize transaction", e)
            throw e
        }
    }
}
```

### Creating Avro Objects

```kotlin
val transaction = TransactionAvro.newBuilder()
    .setDate("2024-12-06")
    .setDescription("Coffee Shop")
    .setAmount("-5.50")
    .setBalance("1234.50")
    .build()

producer.sendTransaction("transaction-events", transaction)
```

### Converting Domain Models

Use the mapper to convert between domain and Avro models:

```kotlin
object TransactionMapper {
    fun toAvro(transaction: Transaction): TransactionAvro {
        return TransactionAvro.newBuilder()
            .setDate(transaction.date)
            .setDescription(transaction.description)
            .setAmount(transaction.amount)
            .setBalance(transaction.balance)
            .build()
    }
    
    fun fromAvro(avro: TransactionAvro): Transaction {
        return Transaction(
            date = avro.getDate(),
            description = avro.getDescription(),
            amount = avro.getAmount(),
            balance = avro.getBalance()
        )
    }
}

// Usage
val domainTransaction = Transaction("2024-12-06", "Coffee", "-5.50", "1234.50")
val avroTransaction = TransactionMapper.toAvro(domainTransaction)
producer.sendTransaction("transaction-events", avroTransaction)
```

## Consuming Avro Messages

### Consumer Configuration

Configure the Kafka consumer in `application.yaml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: pdf-converter-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      properties:
        schema.registry.url: http://localhost:8081
        specific.avro.reader: true
```

**Important**: Set `specific.avro.reader: true` to deserialize to specific Avro classes instead of generic records.

### Consumer Code Example

```kotlin
@Service
class KafkaMessageConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @KafkaListener(
        topics = ["transaction-events"],
        groupId = "pdf-converter-group"
    )
    fun consumeTransaction(
        transaction: TransactionAvro,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        try {
            logger.info("Received transaction from topic {} at offset {}", topic, offset)
            logger.info("Transaction: date={}, description={}, amount={}", 
                transaction.getDate(), 
                transaction.getDescription(), 
                transaction.getAmount()
            )
            
            // Convert to domain model if needed
            val domainTransaction = TransactionMapper.fromAvro(transaction)
            
            // Process the transaction
            processTransaction(domainTransaction)
            
        } catch (e: Exception) {
            logger.error("Failed to process transaction at offset {} in topic {}", 
                offset, topic, e)
        }
    }
    
    private fun processTransaction(transaction: Transaction) {
        // Business logic here
    }
}
```

## Schema Evolution

Schema evolution allows you to modify schemas over time while maintaining compatibility with existing producers and consumers.

### Compatibility Types

#### 1. Backward Compatibility (Recommended)

New schema can read data written with old schema. This is the default and most common mode.

**Rules**:
- ✅ Add fields with default values
- ✅ Remove fields
- ❌ Remove fields without defaults
- ❌ Change field types

**Example**:

```json
// Version 1
{
  "type": "record",
  "name": "TransactionAvro",
  "fields": [
    {"name": "date", "type": "string"},
    {"name": "amount", "type": "string"}
  ]
}

// Version 2 (Backward Compatible)
{
  "type": "record",
  "name": "TransactionAvro",
  "fields": [
    {"name": "date", "type": "string"},
    {"name": "amount", "type": "string"},
    {"name": "category", "type": "string", "default": "uncategorized"}
  ]
}
```

**Use case**: Upgrade consumers first, then producers.

#### 2. Forward Compatibility

Old schema can read data written with new schema.

**Rules**:
- ✅ Remove fields
- ✅ Add fields with defaults
- ❌ Add required fields
- ❌ Change field types

**Use case**: Upgrade producers first, then consumers.

#### 3. Full Compatibility

Both backward and forward compatible. Most restrictive but safest.

**Rules**:
- ✅ Add fields with defaults
- ✅ Remove fields with defaults
- ❌ Change field types
- ❌ Rename fields

**Use case**: When you need maximum flexibility in deployment order.

### Configuring Compatibility Mode

Set compatibility mode via REST API:

```bash
# Set backward compatibility (default)
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "BACKWARD"}' \
  http://localhost:8081/config/transaction-events-value

# Set forward compatibility
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "FORWARD"}' \
  http://localhost:8081/config/transaction-events-value

# Set full compatibility
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "FULL"}' \
  http://localhost:8081/config/transaction-events-value
```

### Testing Compatibility

Test if a new schema is compatible before registering:

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{...new schema...}"}' \
  http://localhost:8081/compatibility/subjects/transaction-events-value/versions/latest
```

Response:
```json
{"is_compatible": true}
```

### Schema Evolution Best Practices

1. **Always add default values** to new fields
2. **Never remove required fields** without a default
3. **Test compatibility** before deploying
4. **Version your schemas** explicitly in documentation
5. **Use backward compatibility** as the default mode
6. **Plan migrations** for breaking changes

### Example: Adding a New Field

1. Update the schema file:

```json
{
  "type": "record",
  "name": "TransactionAvro",
  "namespace": "com.anderson.pdf_converter.avro",
  "fields": [
    {"name": "date", "type": "string"},
    {"name": "description", "type": "string"},
    {"name": "amount", "type": "string"},
    {"name": "balance", "type": ["null", "string"], "default": null},
    {"name": "category", "type": "string", "default": "uncategorized"}
  ]
}
```

2. Rebuild to generate new classes:

```bash
./gradlew clean build
```

3. Update producer code to set the new field:

```kotlin
val transaction = TransactionAvro.newBuilder()
    .setDate("2024-12-06")
    .setDescription("Coffee Shop")
    .setAmount("-5.50")
    .setBalance("1234.50")
    .setCategory("food")
    .build()
```

4. Old consumers will still work (backward compatible)

5. Update consumers to use the new field when ready

## Troubleshooting

### Schema Registry Connection Issues

**Problem**: Producer fails with "Connection refused" to Schema Registry

**Solutions**:
1. Verify Schema Registry is running:
   ```bash
   docker ps | grep schema-registry
   ```

2. Check Schema Registry logs:
   ```bash
   docker logs schema-registry
   ```

3. Verify network connectivity:
   ```bash
   curl http://localhost:8081/subjects
   ```

4. Check application configuration:
   ```yaml
   spring:
     kafka:
       producer:
         properties:
           schema.registry.url: http://localhost:8081  # Correct URL
   ```

### Schema Registration Failures

**Problem**: `SerializationException: Error registering Avro schema`

**Solutions**:
1. Check schema syntax is valid JSON:
   ```bash
   cat src/main/avro/Transaction.avsc | jq .
   ```

2. Verify compatibility mode allows the change:
   ```bash
   curl http://localhost:8081/config/transaction-events-value
   ```

3. Test compatibility before registering:
   ```bash
   curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data '{"schema": "{...}"}' \
     http://localhost:8081/compatibility/subjects/transaction-events-value/versions/latest
   ```

4. Check Schema Registry logs for detailed errors:
   ```bash
   docker logs schema-registry | tail -50
   ```

### Deserialization Failures

**Problem**: Consumer fails with "Unknown magic byte"

**Solutions**:
1. Ensure `specific.avro.reader: true` is set in consumer config

2. Verify the message was produced with Avro serialization (not string)

3. Check that producer and consumer use the same Schema Registry URL

4. Verify schema ID exists in registry:
   ```bash
   curl http://localhost:8081/schemas/ids/1
   ```

**Problem**: Consumer receives `GenericRecord` instead of `TransactionAvro`

**Solution**: Set `specific.avro.reader: true` in consumer properties:
```yaml
spring:
  kafka:
    consumer:
      properties:
        specific.avro.reader: true
```

### Schema Compatibility Errors

**Problem**: `409 Conflict - Schema being registered is incompatible`

**Solutions**:
1. Check what compatibility mode is configured:
   ```bash
   curl http://localhost:8081/config/transaction-events-value
   ```

2. Review the compatibility rules for your mode (see Schema Evolution section)

3. Add default values to new fields:
   ```json
   {"name": "newField", "type": "string", "default": ""}
   ```

4. If breaking change is necessary, use a new topic or subject

### Build Issues

**Problem**: Avro classes not generated

**Solutions**:
1. Verify Avro plugin is configured in `build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
   }
   ```

2. Check schema files are in correct location: `src/main/avro/*.avsc`

3. Run clean build:
   ```bash
   ./gradlew clean build
   ```

4. Check generated files exist:
   ```bash
   ls -la build/generated-main-avro-java/
   ```

**Problem**: `ClassNotFoundException` for generated Avro classes

**Solution**: Ensure generated sources are included in the source set (automatic with Avro plugin)

### Performance Issues

**Problem**: High latency on first message

**Cause**: Schema registration and caching on first use

**Solutions**:
1. Pre-register schemas before starting producers
2. Use schema caching (enabled by default)
3. Increase cache size if needed:
   ```yaml
   spring:
     kafka:
       producer:
         properties:
           schema.registry.cache.capacity: 1000
   ```

**Problem**: Too many Schema Registry requests

**Solution**: Verify schema caching is working. Check logs for repeated registration attempts.

### Common Configuration Mistakes

1. **Wrong serializer class name**:
   ```yaml
   # ❌ Wrong
   value-serializer: org.apache.kafka.common.serialization.AvroSerializer
   
   # ✅ Correct
   value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
   ```

2. **Missing Schema Registry URL**:
   ```yaml
   # Must be in properties section
   spring:
     kafka:
       producer:
         properties:
           schema.registry.url: http://localhost:8081
   ```

3. **Wrong port number**:
   - Schema Registry: 8081 (not 8080)
   - Kafka: 9092
   - Zookeeper: 2181

### Debugging Tips

1. **Enable debug logging**:
   ```yaml
   logging:
     level:
       io.confluent: DEBUG
       org.apache.kafka: DEBUG
   ```

2. **Check message headers** to see schema ID:
   ```kotlin
   @KafkaListener(topics = ["transaction-events"])
   fun consume(
       transaction: TransactionAvro,
       @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) key: String?,
       record: ConsumerRecord<String, TransactionAvro>
   ) {
       logger.info("Schema ID: {}", record.headers())
   }
   ```

3. **Use Schema Registry REST API** to inspect state:
   ```bash
   # List all subjects
   curl http://localhost:8081/subjects
   
   # Get all versions for a subject
   curl http://localhost:8081/subjects/transaction-events-value/versions
   
   # Get specific version
   curl http://localhost:8081/subjects/transaction-events-value/versions/1
   ```

4. **Test with command-line tools**:
   ```bash
   # Produce test message
   docker exec -it kafka kafka-console-producer \
     --bootstrap-server localhost:9092 \
     --topic transaction-events
   
   # Consume messages
   docker exec -it kafka kafka-console-consumer \
     --bootstrap-server localhost:9092 \
     --topic transaction-events \
     --from-beginning
   ```

## Additional Resources

- [Confluent Schema Registry Documentation](https://docs.confluent.io/platform/current/schema-registry/index.html)
- [Apache Avro Specification](https://avro.apache.org/docs/current/spec.html)
- [Schema Evolution and Compatibility](https://docs.confluent.io/platform/current/schema-registry/avro.html)
- [Kafka Avro Serializer](https://docs.confluent.io/platform/current/schema-registry/serdes-develop/serdes-avro.html)

## Summary

This guide covered:
- ✅ Starting Schema Registry with Docker Compose
- ✅ Defining Avro schemas with proper types and documentation
- ✅ Schema registration process and subject naming conventions
- ✅ Producing and consuming Avro messages with code examples
- ✅ Schema evolution strategies (backward, forward, full compatibility)
- ✅ Troubleshooting common issues and configuration mistakes

For questions or issues, refer to the troubleshooting section or consult the official Confluent documentation.
