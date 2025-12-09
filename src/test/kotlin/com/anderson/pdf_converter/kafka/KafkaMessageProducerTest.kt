package com.anderson.pdf_converter.kafka

import com.anderson.pdf_converter.avro.TransactionAvro
import io.mockk.*
import org.apache.kafka.common.errors.SerializationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.core.KafkaTemplate

/**
 * Unit tests for KafkaMessageProducer using MockK.
 * 
 * These tests verify that the producer correctly sends TransactionAvro messages,
 * handles serialization errors, and logs appropriate error messages.
 */
class KafkaMessageProducerTest {

    private val kafkaTemplate: KafkaTemplate<String, TransactionAvro> = mockk(relaxed = true)
    private val producer = KafkaMessageProducer(kafkaTemplate)

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `sendTransaction should send TransactionAvro to correct topic`() {
        // Given
        val topic = "transaction-events"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )

        // When
        producer.sendTransaction(topic, transaction)

        // Then
        verify(exactly = 1) {
            kafkaTemplate.send(topic, transaction)
        }
    }

    @Test
    fun `sendTransaction with key should send TransactionAvro with key to correct topic`() {
        // Given
        val topic = "transaction-events"
        val key = "user-123"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )

        // When
        producer.sendTransaction(topic, key, transaction)

        // Then
        verify(exactly = 1) {
            kafkaTemplate.send(topic, key, transaction)
        }
    }

    @Test
    fun `sendTransaction should handle SerializationException and rethrow`() {
        // Given
        val topic = "transaction-events"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )
        
        every { kafkaTemplate.send(topic, transaction) } throws SerializationException("Schema registration failed")

        // When/Then
        assertThrows<SerializationException> {
            producer.sendTransaction(topic, transaction)
        }

        verify(exactly = 1) {
            kafkaTemplate.send(topic, transaction)
        }
    }

    @Test
    fun `sendTransaction with key should handle SerializationException and rethrow`() {
        // Given
        val topic = "transaction-events"
        val key = "user-123"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )
        
        every { kafkaTemplate.send(topic, key, transaction) } throws SerializationException("Schema registration failed")

        // When/Then
        assertThrows<SerializationException> {
            producer.sendTransaction(topic, key, transaction)
        }

        verify(exactly = 1) {
            kafkaTemplate.send(topic, key, transaction)
        }
    }

    @Test
    fun `sendTransaction should send multiple different transactions`() {
        // Given
        val topic = "transaction-events"
        val transaction1 = TransactionAvro("2024-01-15", "Coffee", "15.50", "1000.00")
        val transaction2 = TransactionAvro("2024-01-16", "Lunch", "25.00", "975.00")
        val transaction3 = TransactionAvro("2024-01-17", "Gas", "50.00", null)

        // When
        producer.sendTransaction(topic, transaction1)
        producer.sendTransaction(topic, transaction2)
        producer.sendTransaction(topic, transaction3)

        // Then
        verify(exactly = 1) { kafkaTemplate.send(topic, transaction1) }
        verify(exactly = 1) { kafkaTemplate.send(topic, transaction2) }
        verify(exactly = 1) { kafkaTemplate.send(topic, transaction3) }
    }

    @Test
    fun `sendTransaction should handle transaction with null balance`() {
        // Given
        val topic = "transaction-events"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            null
        )

        // When
        producer.sendTransaction(topic, transaction)

        // Then
        verify(exactly = 1) {
            kafkaTemplate.send(topic, transaction)
        }
    }

    @Test
    fun `sendTransaction should handle generic exceptions and rethrow`() {
        // Given
        val topic = "transaction-events"
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )
        
        every { kafkaTemplate.send(topic, transaction) } throws RuntimeException("Unexpected error")

        // When/Then
        assertThrows<RuntimeException> {
            producer.sendTransaction(topic, transaction)
        }

        verify(exactly = 1) {
            kafkaTemplate.send(topic, transaction)
        }
    }
}
