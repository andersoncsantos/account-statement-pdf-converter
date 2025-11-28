package com.anderson.pdf_converter.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * Kafka message producer service for publishing messages to Kafka topics.
 * 
 * This service provides a simple interface for sending messages to Kafka.
 * It can be used to publish events such as:
 * - PDF conversion completion events
 * - Processing status updates
 * - Audit trail events
 * 
 * Example usage:
 * ```
 * @Autowired
 * private lateinit var kafkaProducer: KafkaMessageProducer
 * 
 * kafkaProducer.sendMessage("test-topic", "PDF conversion completed")
 * ```
 */
@Service
class KafkaMessageProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    /**
     * Sends a message to the specified Kafka topic.
     * 
     * @param topic The name of the Kafka topic
     * @param message The message content to send
     */
    fun sendMessage(topic: String, message: String) {
        println("Publishing message to Kafka topic: $topic")
        kafkaTemplate.send(topic, message)
        println("Message published successfully")
    }

    /**
     * Sends a message with a key to the specified Kafka topic.
     * Messages with the same key will be sent to the same partition.
     * 
     * @param topic The name of the Kafka topic
     * @param key The message key (used for partitioning)
     * @param message The message content to send
     */
    fun sendMessage(topic: String, key: String, message: String) {
        println("Publishing message to Kafka topic: $topic with key: $key")
        kafkaTemplate.send(topic, key, message)
        println("Message published successfully")
    }
}
