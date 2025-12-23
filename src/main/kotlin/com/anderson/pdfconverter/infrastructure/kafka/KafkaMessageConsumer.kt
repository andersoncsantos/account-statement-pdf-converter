package com.anderson.pdfconverter.infrastructure.kafka

import com.anderson.pdf_converter.avro.TransactionAvro
import org.apache.kafka.common.errors.SerializationException
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * Kafka message consumer that listens to Transaction messages from Kafka topics.
 *
 * This service consumes Avro-serialized Transaction messages with Schema Registry integration:
 * - Connects to the local Kafka broker (configured in application.yaml)
 * - Subscribes to the "transaction-events" topic
 * - Deserializes messages using KafkaAvroDeserializer with Schema Registry
 * - Handles deserialization failures with detailed error logging
 *
 * To test this consumer:
 * 1. Ensure Kafka and Schema Registry are running: docker-compose up -d
 * 2. Start the Spring Boot application
 * 3. Publish a Transaction message using KafkaMessageProducer
 * 4. Check the application logs to see the consumed transaction
 */
@Service
class KafkaMessageConsumer {

    private val logger = LoggerFactory.getLogger(KafkaMessageConsumer::class.java)

    /**
     * Consumes Transaction messages from the "transaction-events" Kafka topic.
     *
     * Messages are automatically deserialized from Avro format using the schema
     * registered in the Schema Registry. Deserialization failures are logged
     * with full context including topic, partition, and offset information.
     *
     * @param transaction The deserialized Transaction message
     * @param topic The topic name from which the message was consumed
     * @param partition The partition number
     * @param offset The message offset
     */
    @KafkaListener(topics = ["transaction-events"], groupId = "pdf-converter-group")
    fun consume(
        @Payload transaction: TransactionAvro,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        try {
            logger.info("========================================")
            logger.info("Transaction Message Received:")
            logger.info("Topic: $topic")
            logger.info("Partition: $partition")
            logger.info("Offset: $offset")
            logger.info("Date: ${transaction.date}")
            logger.info("Description: ${transaction.description}")
            logger.info("Amount: ${transaction.amount}")
            logger.info("Balance: ${transaction.balance ?: "N/A"}")
            logger.info("Timestamp: ${System.currentTimeMillis()}")
            logger.info("========================================")

            // Process the transaction here
            // For now, we just log it

        } catch (e: SerializationException) {
            logger.error(
                "Deserialization failed for message at offset $offset in topic $topic (partition $partition)",
                e
            )
            // Message is skipped, processing continues with next message
        } catch (e: Exception) {
            logger.error(
                "Error processing message at offset $offset in topic $topic (partition $partition)",
                e
            )
            // Message is skipped, processing continues with next message
        }
    }
}