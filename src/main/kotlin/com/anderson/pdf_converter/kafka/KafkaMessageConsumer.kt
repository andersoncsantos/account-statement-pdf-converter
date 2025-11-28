package com.anderson.pdf_converter.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/**
 * Kafka message consumer that listens to messages from a Kafka topic.
 * 
 * This service demonstrates basic Kafka consumer functionality by:
 * - Connecting to the local Kafka broker (configured in application.yaml)
 * - Subscribing to the "test-topic" topic
 * - Printing received messages to the console
 * 
 * To test this consumer:
 * 1. Ensure Kafka is running: docker-compose up -d
 * 2. Start the Spring Boot application
 * 3. Publish a message: ./scripts/produce-message.sh test-topic "Hello from Kafka!"
 * 4. Check the application logs to see the consumed message
 */
@Service
class KafkaMessageConsumer {

    /**
     * Consumes messages from the "test-topic" Kafka topic.
     * 
     * @param message The message content received from Kafka
     */
    @KafkaListener(topics = ["test-topic"], groupId = "pdf-converter-group")
    fun consume(message: String) {
        println("========================================")
        println("Kafka Message Received:")
        println("Topic: test-topic")
        println("Message: $message")
        println("Timestamp: ${System.currentTimeMillis()}")
        println("========================================")
    }
}
