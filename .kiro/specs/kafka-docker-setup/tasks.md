# Implementation Plan

- [x] 1. Create Docker Compose configuration file
  - Create `docker-compose.yml` in project root with Zookeeper and Kafka services
  - Configure Zookeeper service with port 2181 and necessary environment variables
  - Configure Kafka broker service with port 9092, Zookeeper connection, and broker settings
  - Set up service dependencies to ensure Zookeeper starts before Kafka
  - Configure environment variables: KAFKA_BROKER_ID, KAFKA_ZOOKEEPER_CONNECT, KAFKA_ADVERTISED_LISTENERS, KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
  - _Requirements: 1.1, 1.2, 1.3, 1.5_

- [x] 2. Create topic creation script
  - Create `scripts/create-topic.sh` with executable permissions
  - Implement parameter handling for topic name, partitions, and replication factor
  - Set default values: topic name "test-topic", 1 partition, replication factor 1
  - Use docker exec to run kafka-topics command inside Kafka container
  - Add error handling for duplicate topic creation
  - Add topic existence verification after creation
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 3. Create message producer script
  - Create `scripts/produce-message.sh` with executable permissions
  - Implement parameter handling for topic name and message content
  - Set default values: topic name "test-topic", sample message text
  - Use docker exec to run kafka-console-producer command inside Kafka container
  - Add confirmation output when message is published successfully
  - Add error handling and descriptive error messages for failures
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4. Create message consumer script
  - Create `scripts/consume-messages.sh` with executable permissions
  - Implement parameter handling for topic name
  - Set default value: topic name "test-topic"
  - Configure consumer to read from beginning of topic
  - Use docker exec to run kafka-console-consumer command inside Kafka container
  - Display consumed messages to stdout for verification
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 5. Create comprehensive documentation
  - Create `KAFKA_SETUP.md` in project root
  - Document prerequisites: Docker and Docker Compose installation
  - Provide step-by-step instructions for starting the environment with docker-compose up
  - Include testing procedure with example commands for topic creation, message production, and consumption
  - Document all configuration parameters and their purposes
  - Add troubleshooting section for common issues (port conflicts, Docker not running, insufficient resources)
  - Include instructions for stopping the environment with docker-compose down
  - Add section on Spring Boot integration with Kafka dependency and configuration examples
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 6.2_

- [x] 6. Create end-to-end testing workflow script
  - Create `scripts/test-kafka-setup.sh` that orchestrates the complete test workflow
  - Start Docker Compose environment
  - Wait for Kafka to be ready (health check with retry logic)
  - Execute topic creation script
  - Execute producer script to publish test message
  - Execute consumer script to retrieve and verify message
  - Display success/failure status for each step
  - _Requirements: 5.3_

- [x]* 7. Add Spring Boot Kafka integration example
  - Update `build.gradle.kts` to include spring-kafka dependencies
  - Update `application.yaml` with Kafka configuration (bootstrap-servers, consumer/producer settings)
  - Create example Kafka producer service class
  - Create example Kafka consumer service class
  - Add example usage in PDF converter workflow (optional event publishing)
  - _Requirements: 6.1, 6.2_

- [ ]* 8. Write integration tests for Kafka setup
  - Create test class for Docker Compose environment validation
  - Test container startup and port accessibility (Properties 1, 2)
  - Test topic creation and idempotence (Properties 3, 4)
  - Test message round-trip (Property 5)
  - Test producer confirmation (Property 6)
  - Test error reporting (Property 7)
  - Test consumer subscription (Property 8)
  - Test clean shutdown (Property 10)
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.3, 2.4, 3.1, 3.3, 3.4, 4.1, 6.4_

- [ ]* 9. Write Spring Boot integration tests
  - Create test class using Testcontainers for Kafka
  - Test application connectivity to Kafka broker (Property 9)
  - Test connection error handling when Kafka is unavailable (Property 6.3)
  - Test producer functionality from Spring Boot application
  - Test consumer functionality from Spring Boot application
  - _Requirements: 6.1, 6.3_

- [x] 10. Final verification and documentation review
  - Verify all scripts have executable permissions
  - Test complete workflow on clean environment
  - Verify all documentation steps are accurate
  - Ensure all configuration parameters are documented
  - Confirm error messages are clear and helpful
  - _Requirements: All_
