# Kafka Local Development Setup

This guide provides step-by-step instructions for setting up and testing a local Apache Kafka instance using Docker Compose for the PDF Converter application.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Docker**: Version 20.10 or higher
  - [Install Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: Version 2.0 or higher (usually included with Docker Desktop)
  - Verify with: `docker-compose --version`

### System Requirements

- Minimum 2GB RAM allocated to Docker
- Ports 9092 (Kafka) and 2181 (Zookeeper) must be available

## Quick Start

### 1. Start the Kafka Environment

From the project root directory, run:

```bash
docker compose up -d
```

This command starts both Zookeeper and Kafka broker in detached mode.

**Verify containers are running:**

```bash
docker ps
```

You should see two containers: `zookeeper` and `kafka`.

**Check logs to ensure successful startup:**

```bash
# View Kafka logs
docker logs kafka

# View Zookeeper logs
docker logs zookeeper
```

Wait about 30 seconds for Kafka to fully initialize before proceeding.

### 2. Create a Test Topic

Make the script executable (first time only):

```bash
chmod +x scripts/create-topic.sh
```

Create a topic named `test-topic`:

```bash
./scripts/create-topic.sh
```

**With custom parameters:**

```bash
./scripts/create-topic.sh my-topic 3 1
# Creates topic "my-topic" with 3 partitions and replication factor 1
```

### 3. Publish a Test Message

Make the script executable (first time only):

```bash
chmod +x scripts/produce-message.sh
```

Publish a message to the topic:

```bash
./scripts/produce-message.sh
```

**With custom message:**

```bash
./scripts/produce-message.sh test-topic "Hello from Kafka!"
```

### 4. Consume Messages

Make the script executable (first time only):

```bash
chmod +x scripts/consume-messages.sh
```

Read messages from the topic:

```bash
  ./scripts/consume-messages.sh
```

**Consume from a specific topic:**

```bash
./scripts/consume-messages.sh my-topic
```

Press `Ctrl+C` to stop the consumer.

## Complete Testing Workflow

Run this complete workflow to verify your setup:

```bash
# 1. Start the environment
docker compose up -d

# 2. Wait for Kafka to be ready (30 seconds)
sleep 30

# 3. Create a topic
./scripts/create-topic.sh test-topic

# 4. Publish a message
./scripts/produce-message.sh test-topic "Test message 1"

# 5. Consume messages (in a new terminal)
./scripts/consume-messages.sh test-topic
```

## Configuration Details

### Docker Compose Services

#### Zookeeper
- **Port**: 2181
- **Purpose**: Manages Kafka cluster metadata and coordination
- **Image**: confluentinc/cp-zookeeper:7.5.0

#### Kafka Broker
- **Port**: 9092
- **Purpose**: Handles message storage and serving
- **Image**: confluentinc/cp-kafka:7.5.0

### Environment Variables

| Variable | Value | Purpose |
|----------|-------|---------|
| `KAFKA_BROKER_ID` | 1 | Unique identifier for the broker |
| `KAFKA_ZOOKEEPER_CONNECT` | zookeeper:2181 | Connection string to Zookeeper |
| `KAFKA_ADVERTISED_LISTENERS` | PLAINTEXT://localhost:9092 | How clients connect to Kafka |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` | 1 | Replication for internal topics (1 for single broker) |

## Spring Boot Integration

### Add Kafka Dependencies

Add to `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

### Configure Application

Add to `src/main/resources/application.yaml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: pdf-converter-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### Example Kafka Consumer

The project includes a `KafkaMessageConsumer` class that demonstrates consuming messages from Kafka:

```kotlin
@Service
class KafkaMessageConsumer {
    
    @KafkaListener(topics = ["test-topic"], groupId = "pdf-converter-group")
    fun consume(message: String) {
        println("Received message: $message")
    }
}
```

To use it:
1. Ensure Kafka is running (`docker compose up -d`)
2. Start your Spring Boot application
3. Publish messages using the producer script
4. Watch the application logs to see consumed messages

## Troubleshooting

### Port Already in Use

**Problem**: Error binding to port 9092 or 2181

**Solution**: 
- Check if another process is using the port: `lsof -i :9092` or `netstat -an | grep 9092`
- Stop the conflicting service or modify the port in `docker compose.yml`

### Docker Not Running

**Problem**: Cannot connect to Docker daemon

**Solution**:
- Start Docker Desktop (macOS/Windows)
- Start Docker service: `sudo systemctl start docker` (Linux)

### Kafka Not Ready

**Problem**: Scripts fail with connection errors

**Solution**:
- Wait 30-60 seconds after starting containers
- Check container status: `docker ps`
- View logs: `docker logs kafka`
- Restart if needed: `docker compose restart kafka`

### Insufficient Memory

**Problem**: Containers start but crash repeatedly

**Solution**:
- Increase Docker memory allocation to at least 2GB
- Docker Desktop: Preferences → Resources → Memory

### Topic Already Exists

**Problem**: Error when creating a topic that already exists

**Solution**:
- This is handled gracefully by the `--if-not-exists` flag
- List existing topics: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`
- Delete a topic if needed: `docker exec kafka kafka-topics --delete --topic test-topic --bootstrap-server localhost:9092`

### Consumer Not Receiving Messages

**Problem**: Consumer script runs but shows no messages

**Solution**:
- Verify topic exists: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`
- Check if messages were published: `docker exec kafka kafka-console-consumer --topic test-topic --from-beginning --bootstrap-server localhost:9092 --max-messages 1`
- Ensure you're using the correct topic name

## Stopping the Environment

To stop and remove all containers:

```bash
docker compose down
```

To stop containers but keep data:

```bash
docker compose stop
```

To restart:

```bash
docker compose start
```

## Advanced Usage

### List All Topics

```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Describe a Topic

```bash
docker exec kafka kafka-topics --describe --topic test-topic --bootstrap-server localhost:9092
```

### Delete a Topic

```bash
docker exec kafka kafka-topics --delete --topic test-topic --bootstrap-server localhost:9092
```

### View Consumer Groups

```bash
docker exec kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

## Next Steps

1. **Integrate with PDF Converter**: Publish events when PDFs are converted
2. **Add Schema Registry**: For structured message formats (Avro, Protobuf)
3. **Add Kafka UI**: Install a web-based UI for visual topic management
4. **Implement Error Handling**: Add dead letter queues for failed messages
5. **Add Monitoring**: Set up Prometheus and Grafana for metrics

## Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Confluent Docker Images](https://docs.confluent.io/platform/current/installation/docker/image-reference.html)
