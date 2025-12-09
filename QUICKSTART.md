# Quick Start Guide - Kafka Setup

This guide will get you up and running with Kafka in under 5 minutes.

## Prerequisites

- Docker and Docker Compose installed
- Bash shell (Linux, macOS, or Git Bash on Windows)

## Step 1: Make Scripts Executable

```bash
chmod +x scripts/*.sh
```

Or use the setup script:

```bash
bash scripts/setup-permissions.sh
```

## Step 2: Start Kafka

```bash
docker compose up -d
```

Wait about 30 seconds for Kafka to fully start.

## Step 3: Run the Complete Test

```bash
./scripts/test-kafka-setup.sh
```

This script will:
- ✓ Verify Kafka is running
- ✓ Create a test topic
- ✓ Publish a test message
- ✓ Consume and verify the message

## Step 4: Test with Spring Boot Application

### Option A: Using Gradle

```bash
# Terminal 1: Start the application
./gradlew bootRun

# Terminal 2: Publish a message
./scripts/produce-message.sh test-topic "Hello from Spring Boot!"

# Check Terminal 1 logs - you should see the consumed message
```

### Option B: Using JAR

```bash
# Build the application
./gradlew bootJar

# Terminal 1: Run the JAR
java -jar build/libs/pdf-converter-0.0.1-SNAPSHOT.jar

# Terminal 2: Publish a message
./scripts/produce-message.sh test-topic "Hello from Spring Boot!"

# Check Terminal 1 logs - you should see the consumed message
```

## Manual Testing

### Create a Topic

```bash
./scripts/create-topic.sh my-custom-topic
```

### Publish Messages

```bash
./scripts/produce-message.sh my-custom-topic "First message"
./scripts/produce-message.sh my-custom-topic "Second message"
./scripts/produce-message.sh my-custom-topic "Third message"
```

### Consume Messages

```bash
./scripts/consume-messages.sh my-custom-topic
```

Press `Ctrl+C` to stop consuming.

## Stopping Kafka

```bash
docker compose down
```

## Troubleshooting

### Port Already in Use

If you get a port conflict error:

```bash
# Check what's using port 9092
lsof -i :9092

# Or on Linux
netstat -tulpn | grep 9092
```

### Kafka Not Ready

If scripts fail with connection errors:

```bash
# Check container status
docker ps

# View Kafka logs
docker logs kafka

# Restart Kafka
docker compose restart kafka
```

### Scripts Not Executable

If you get "Permission denied":

```bash
chmod +x scripts/*.sh
```

## Next Steps

- Read [KAFKA_SETUP.md](KAFKA_SETUP.md) for detailed documentation
- Explore the `KafkaMessageConsumer` class in `src/main/kotlin/com/anderson/pdf_converter/kafka/`
- Integrate Kafka events into your PDF conversion workflow

## Useful Commands

```bash
# List all topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe a topic
docker exec kafka kafka-topics --describe --topic test-topic --bootstrap-server localhost:9092

# Delete a topic
docker exec kafka kafka-topics --delete --topic test-topic --bootstrap-server localhost:9092

# View consumer groups
docker exec kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

## Success Indicators

You'll know everything is working when:

1. ✓ `docker ps` shows both `kafka` and `zookeeper` containers running
2. ✓ `./scripts/test-kafka-setup.sh` completes without errors
3. ✓ Spring Boot application logs show "Received message: ..." when you publish messages
4. ✓ `./scripts/consume-messages.sh` displays messages you published

## Getting Help

- Check [KAFKA_SETUP.md](KAFKA_SETUP.md) for detailed troubleshooting
- View container logs: `docker logs kafka` or `docker logs zookeeper`
- Verify Docker is running: `docker info`
