package com.anderson.pdfconverter.infrastructure.kafka

import com.anderson.pdf_converter.avro.TransactionAvro
import org.apache.kafka.common.errors.SerializationException
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * Kafka message producer service for publishing Avro-serialized messages to Kafka topics.
 *
 * This service provides a simple interface for sending Transaction messages to Kafka
 * using Avro serialization with Schema Registry integration.
 *
 * The producer automatically registers schemas with the Schema Registry on first use
 * and validates all messages against the registered schema before sending.
 *
 * Example usage:
 * ```
 * @Autowired
 * private lateinit var kafkaProducer: KafkaMessageProducer
 *
 * val transaction = TransactionAvro("2024-01-01", "Purchase", "100.00", "500.00")
 * kafkaProducer.sendTransaction("transaction-events", transaction)
 * ```
 */
@Service
class KafkaMessageProducer(
    private val kafkaTemplate: KafkaTemplate<String, TransactionAvro>
) {
    private val logger = LoggerFactory.getLogger(KafkaMessageProducer::class.java)

    /**
     * Sends a Transaction message to the specified Kafka topic.
     *
     * The message is serialized using Avro format and validated against the schema
     * registered in the Schema Registry. If the schema doesn't exist, it will be
     * automatically registered on first use.
     *
     * @param topic The name of the Kafka topic
     * @param transaction The TransactionAvro object to send
     * @throws org.apache.kafka.common.errors.SerializationException if schema registration or serialization fails
     */
    fun sendTransaction(topic: String, transaction: TransactionAvro) {
        try {
            logger.info("Publishing transaction to Kafka topic: {}", topic)
            kafkaTemplate.send(topic, transaction)
            logger.info("Transaction published successfully to topic: {}", topic)
        } catch (e: SerializationException) {
            logger.error(
                "Failed to serialize transaction for topic: {}. " +
                "This may indicate a schema registration failure or schema validation error. " +
                "Error: {}",
                topic,
                e.message,
                e
            )
            throw e
        } catch (e: Exception) {
            logger.error(
                "Unexpected error while publishing transaction to topic: {}. Error: {}",
                topic,
                e.message,
                e
            )
            throw e
        }
    }

    /**
     * Sends a Transaction message with a key to the specified Kafka topic.
     * Messages with the same key will be sent to the same partition.
     *
     * The message is serialized using Avro format and validated against the schema
     * registered in the Schema Registry. If the schema doesn't exist, it will be
     * automatically registered on first use.
     *
     * @param topic The name of the Kafka topic
     * @param key The message key (used for partitioning)
     * @param transaction The TransactionAvro object to send
     * @throws SerializationException if schema registration or serialization fails
     */
    fun sendTransaction(topic: String, key: String, transaction: TransactionAvro) {
        try {
            logger.info("Publishing transaction to Kafka topic: {} with key: {}", topic, key)
            kafkaTemplate.send(topic, key, transaction)
            logger.info("Transaction published successfully to topic: {} with key: {}", topic, key)
        } catch (e: SerializationException) {
            logger.error(
                "Failed to serialize transaction for topic: {} with key: {}. " +
                "This may indicate a schema registration failure or schema validation error. " +
                "Error: {}",
                topic,
                key,
                e.message,
                e
            )
            throw e
        } catch (e: Exception) {
            logger.error(
                "Unexpected error while publishing transaction to topic: {} with key: {}. Error: {}",
                topic,
                key,
                e.message,
                e
            )
            throw e
        }
    }
}