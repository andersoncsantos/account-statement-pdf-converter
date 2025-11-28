# Design Document

## Overview

This design provides a complete Apache Kafka local development environment using Docker Compose. The solution includes Kafka broker, Zookeeper coordination service, and testing utilities (shell scripts) to verify the setup. The design ensures out-of-the-box functionality with minimal configuration, enabling developers to quickly provision and test Kafka messaging capabilities for the PDF Converter application.

The architecture uses official Confluent Kafka Docker images, which provide production-grade Kafka with simplified configuration. Testing scripts leverage Kafka's built-in command-line tools to create topics, produce messages, and consume messages.

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────┐
│                   Docker Compose                         │
│                                                          │
│  ┌──────────────┐              ┌──────────────┐        │
│  │  Zookeeper   │◄─────────────┤    Kafka     │        │
│  │   :2181      │  Coordination│   Broker     │        │
│  │              │              │   :9092      │        │
│  └──────────────┘              └──────┬───────┘        │
│                                        │                 │
└────────────────────────────────────────┼────────────────┘
                                         │
                    ┌────────────────────┼────────────────┐
                    │                    │                 │
              ┌─────▼─────┐      ┌──────▼──────┐   ┌─────▼─────┐
              │  Topic     │      │  Producer   │   │ Consumer  │
              │  Creation  │      │  Script     │   │  Script   │
              │  Script    │      │             │   │           │
              └────────────┘      └─────────────┘   └───────────┘
```

### Container Architecture

1. **Zookeeper Container**: Manages Kafka cluster metadata and coordination
2. **Kafka Broker Container**: Handles message storage and serving
3. **Host Machine**: Runs testing scripts that connect to Kafka via exposed ports

### Network Configuration

- Zookeeper and Kafka communicate via Docker internal network
- Kafka broker is accessible from host machine on `localhost:9092`
- Zookeeper is accessible from host machine on `localhost:2181`

## Components and Interfaces

### 1. Docker Compose Configuration

**File**: `docker-compose.yml`

**Services**:
- `zookeeper`: Confluent Zookeeper image
  - Port: 2181 (client connections)
  - Environment: Tick time, client port, max connections
  
- `kafka`: Confluent Kafka image
  - Port: 9092 (broker connections)
  - Dependencies: Zookeeper must start first
  - Environment: Zookeeper connection, listener configuration, broker ID, offsets topic replication

**Configuration Parameters**:
- `KAFKA_BROKER_ID`: Unique identifier for the broker (set to 1)
- `KAFKA_ZOOKEEPER_CONNECT`: Connection string to Zookeeper (zookeeper:2181)
- `KAFKA_ADVERTISED_LISTENERS`: How clients connect (PLAINTEXT://localhost:9092)
- `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR`: Replication for internal topics (set to 1 for single broker)

### 2. Topic Creation Script

**File**: `scripts/create-topic.sh`

**Purpose**: Creates a Kafka topic for testing

**Interface**:
```bash
./scripts/create-topic.sh [topic-name] [partitions] [replication-factor]
```

**Default Values**:
- Topic name: `test-topic`
- Partitions: 1
- Replication factor: 1

**Implementation**: Uses `kafka-topics` command-line tool via docker exec

### 3. Producer Script

**File**: `scripts/produce-message.sh`

**Purpose**: Publishes test messages to a Kafka topic

**Interface**:
```bash
./scripts/produce-message.sh [topic-name] [message]
```

**Default Values**:
- Topic name: `test-topic`
- Message: "Hello Kafka! This is a test message."

**Implementation**: Uses `kafka-console-producer` command-line tool via docker exec

### 4. Consumer Script

**File**: `scripts/consume-messages.sh`

**Purpose**: Consumes and displays messages from a Kafka topic

**Interface**:
```bash
./scripts/consume-messages.sh [topic-name]
```

**Default Values**:
- Topic name: `test-topic`
- Reads from beginning of topic

**Implementation**: Uses `kafka-console-consumer` command-line tool via docker exec

### 5. Documentation

**File**: `KAFKA_SETUP.md`

**Contents**:
- Prerequisites (Docker, Docker Compose)
- Step-by-step startup instructions
- Testing procedure with example commands
- Troubleshooting guide
- Integration instructions for Spring Boot application

## Data Models

### Kafka Topic Configuration

```
Topic:
  - name: string (e.g., "test-topic")
  - partitions: integer (default: 1)
  - replication-factor: integer (default: 1)
  - retention: time-based (Kafka default: 7 days)
```

### Message Format

For testing purposes, messages are simple strings. The design supports any message format since Kafka treats messages as byte arrays.

```
Message:
  - key: optional string
  - value: string (test message content)
  - timestamp: auto-generated by Kafka
  - partition: auto-assigned by Kafka
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Container startup dependency order

*For any* Docker Compose execution, when starting the services, Zookeeper must reach a healthy state before Kafka broker attempts to connect
**Validates: Requirements 1.1**

### Property 2: Port accessibility

*For any* successfully started Kafka environment, connections to localhost:9092 and localhost:2181 must be accepted
**Validates: Requirements 1.2, 1.3**

### Property 3: Topic creation idempotence

*For any* topic name, creating the same topic multiple times should result in either successful creation on first attempt or graceful handling on subsequent attempts without system failure
**Validates: Requirements 2.3**

### Property 4: Topic existence verification

*For any* topic that is successfully created, querying the broker for topic list must include that topic name
**Validates: Requirements 2.4**

### Property 5: Message round-trip consistency

*For any* message published to a topic, consuming from that topic from the beginning should retrieve a message with identical content
**Validates: Requirements 3.1, 3.2, 4.2, 4.4**

### Property 6: Producer confirmation

*For any* successful message publication, the producer must provide acknowledgment or confirmation output
**Validates: Requirements 3.3**

### Property 7: Error reporting

*For any* failed operation (topic creation, message production, message consumption), the system must output descriptive error information
**Validates: Requirements 3.4**

### Property 8: Consumer subscription

*For any* existing topic with messages, starting a consumer for that topic must successfully subscribe and begin retrieving messages
**Validates: Requirements 4.1**

### Property 9: Application connectivity

*For any* Spring Boot application configured with the Kafka broker address (localhost:9092), the application must successfully establish a connection when Kafka is running
**Validates: Requirements 6.1, 6.2**

### Property 10: Clean shutdown

*For any* running Docker Compose environment, executing docker-compose down must stop all containers and remove them without leaving orphaned processes
**Validates: Requirements 6.4**

## Error Handling

### Docker Compose Errors

1. **Port Conflicts**: If ports 9092 or 2181 are already in use
   - Detection: Docker Compose will fail with port binding error
   - Resolution: Documentation will guide users to stop conflicting services or modify ports in docker-compose.yml

2. **Docker Not Running**: If Docker daemon is not started
   - Detection: Docker Compose command fails immediately
   - Resolution: Documentation includes prerequisite check instructions

3. **Insufficient Resources**: If Docker has insufficient memory allocated
   - Detection: Containers start but crash repeatedly
   - Resolution: Documentation specifies minimum Docker resource requirements (2GB RAM recommended)

### Kafka Operation Errors

1. **Topic Already Exists**: When creating a duplicate topic
   - Handling: Script checks exit code and provides friendly message
   - Behavior: Non-fatal, continues execution

2. **Broker Not Ready**: When executing commands before Kafka is fully started
   - Handling: Scripts include retry logic with timeout
   - Behavior: Wait and retry up to 30 seconds

3. **Connection Refused**: When Kafka broker is not accessible
   - Detection: Connection timeout or refused error
   - Resolution: Documentation includes troubleshooting steps to verify container status

4. **Message Production Failure**: When producer cannot write to topic
   - Handling: Script captures error output and displays to user
   - Behavior: Exit with non-zero status code

### Script Errors

1. **Missing Parameters**: When required arguments are not provided
   - Handling: Scripts use default values and display usage information
   - Behavior: Continue with defaults or exit with usage message

2. **Container Not Running**: When scripts try to exec into stopped containers
   - Detection: Docker exec returns error
   - Resolution: Error message instructs user to start Docker Compose environment

## Testing Strategy

### Manual Integration Testing

The primary testing approach is manual integration testing using the provided scripts. This validates the complete setup from container startup through message flow.

**Test Scenarios**:

1. **Fresh Environment Setup**
   - Start Docker Compose
   - Verify both containers are running
   - Check logs for successful startup

2. **Topic Management**
   - Create a new topic
   - Verify topic exists
   - Attempt to create duplicate topic

3. **Message Flow**
   - Produce a message to topic
   - Consume message from topic
   - Verify message content matches

4. **Error Conditions**
   - Attempt operations with Kafka stopped
   - Try to connect to wrong port
   - Verify error messages are clear

### Automated Testing Approach

While the primary deliverable is manual testing scripts, the design supports future automated testing:

**Unit Tests** (Future Enhancement):
- Test script parameter parsing
- Test default value handling
- Test error message formatting

**Property-Based Tests** (Future Enhancement):

Property-based testing would be valuable for validating Kafka behavior across many inputs. For this feature, we would use a Kotlin property-based testing library such as Kotest Property Testing.

Example properties to test:
- Message round-trip for arbitrary string content
- Topic creation with various valid names
- Consumer retrieval of multiple messages in order

**Integration Tests** (Future Enhancement):
- Spring Boot test that starts test containers
- Verify application can connect to Kafka
- Test producer/consumer within application context

### Testing Configuration

For future automated tests:
- Use Testcontainers library for Kafka in tests
- Configure test Kafka with same settings as docker-compose.yml
- Ensure tests clean up resources after execution

### Documentation Testing

All documentation steps must be manually verified:
- Follow README instructions on clean machine
- Verify all commands execute successfully
- Confirm output matches documented examples
- Test troubleshooting steps resolve common issues

## Implementation Notes

### Technology Choices

1. **Confluent Kafka Images**: Using `confluentinc/cp-kafka` and `confluentinc/cp-zookeeper`
   - Reason: Well-maintained, production-grade, extensive documentation
   - Alternative considered: Apache Kafka official images (less configuration options)

2. **Shell Scripts**: Using bash scripts for testing utilities
   - Reason: Cross-platform (works on Linux, macOS, Git Bash on Windows), no additional dependencies
   - Alternative considered: Kotlin scripts (would require Gradle execution)

3. **Single Broker Setup**: One Kafka broker, one Zookeeper instance
   - Reason: Sufficient for local development, minimal resource usage
   - Alternative considered: Multi-broker cluster (unnecessary complexity for local dev)

### Configuration Decisions

1. **Replication Factor**: Set to 1
   - Reason: Single broker cannot replicate
   - Production: Would use 3 for fault tolerance

2. **Partitions**: Default to 1
   - Reason: Simplifies testing, adequate for development
   - Production: Would use multiple partitions for parallelism

3. **Port Mapping**: Standard Kafka ports
   - Reason: Matches production conventions, easier for developers familiar with Kafka
   - Alternative: Could use non-standard ports to avoid conflicts

### Future Enhancements

1. **Kafka UI**: Add a web-based UI container (e.g., kafka-ui) for visual topic management
2. **Schema Registry**: Add Confluent Schema Registry for Avro message schemas
3. **Multiple Environments**: Provide docker-compose profiles for different configurations
4. **Spring Boot Integration**: Add Kafka producer/consumer examples in the PDF Converter application
5. **Monitoring**: Add Prometheus and Grafana for Kafka metrics visualization

## Integration with PDF Converter Application

### Spring Boot Kafka Dependencies

To integrate Kafka with the existing Spring Boot application, add to `build.gradle.kts`:

```kotlin
implementation("org.springframework.kafka:spring-kafka")
testImplementation("org.springframework.kafka:spring-kafka-test")
```

### Application Configuration

Add to `application.yaml`:

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

### Example Use Cases

1. **Event Publishing**: When a PDF is converted, publish an event to a topic
2. **Async Processing**: Consume PDF conversion requests from a topic
3. **Audit Trail**: Publish all conversion activities to an audit topic
4. **Notifications**: Publish completion events for downstream systems

These use cases are not implemented in this feature but are enabled by the Kafka infrastructure.
