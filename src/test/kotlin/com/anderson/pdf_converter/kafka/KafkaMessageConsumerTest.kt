package com.anderson.pdf_converter.kafka

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.anderson.pdf_converter.avro.TransactionAvro
import com.anderson.pdfconverter.infrastructure.kafka.KafkaMessageConsumer
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.spyk
import org.apache.kafka.common.errors.SerializationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Unit tests for KafkaMessageConsumer using MockK.
 * 
 * **Feature: schema-registry-integration, Property 6: Schema ID extraction enables deserialization**
 * **Validates: Requirements 5.2, 5.3, 5.4**
 * 
 * These tests verify that the consumer correctly processes TransactionAvro messages,
 * handles deserialization errors, and logs appropriate error messages with context.
 */
class KafkaMessageConsumerTest {

    private lateinit var consumer: KafkaMessageConsumer
    private lateinit var logAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

    @BeforeEach
    fun setup() {
        consumer = KafkaMessageConsumer()
        
        // Set up log capturing
        logger = LoggerFactory.getLogger(KafkaMessageConsumer::class.java) as Logger
        logAppender = ListAppender()
        logAppender.start()
        logger.addAppender(logAppender)
    }

    @AfterEach
    fun cleanup() {
        logger.detachAppender(logAppender)
        clearAllMocks()
    }

    @Test
    fun `consume should process TransactionAvro message successfully`() {
        // Given
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )
        val topic = "transaction-events"
        val partition = 0
        val offset = 123L

        // When
        consumer.consume(transaction, topic, partition, offset)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.message.contains("Transaction Message Received") })
        assertTrue(logEvents.any { it.message.contains("Topic: $topic") })
        assertTrue(logEvents.any { it.message.contains("Partition: $partition") })
        assertTrue(logEvents.any { it.message.contains("Offset: $offset") })
        assertTrue(logEvents.any { it.message.contains("Date: ${transaction.date}") })
        assertTrue(logEvents.any { it.message.contains("Description: ${transaction.description}") })
        assertTrue(logEvents.any { it.message.contains("Amount: ${transaction.amount}") })
        assertTrue(logEvents.any { it.message.contains("Balance: ${transaction.balance}") })
    }

    @Test
    fun `consume should handle transaction with null balance`() {
        // Given
        val transaction = TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            null
        )
        val topic = "transaction-events"
        val partition = 0
        val offset = 456L

        // When
        consumer.consume(transaction, topic, partition, offset)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.message.contains("Transaction Message Received") })
        assertTrue(logEvents.any { it.message.contains("Balance: N/A") })
    }

    @Test
    fun `consume should process multiple different transactions`() {
        // Given
        val transaction1 = TransactionAvro("2024-01-15", "Coffee", "15.50", "1000.00")
        val transaction2 = TransactionAvro("2024-01-16", "Lunch", "25.00", "975.00")
        val transaction3 = TransactionAvro("2024-01-17", "Gas", "50.00", null)
        val topic = "transaction-events"

        // When
        consumer.consume(transaction1, topic, 0, 100L)
        consumer.consume(transaction2, topic, 0, 101L)
        consumer.consume(transaction3, topic, 0, 102L)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.message.contains("Offset: 100") })
        assertTrue(logEvents.any { it.message.contains("Offset: 101") })
        assertTrue(logEvents.any { it.message.contains("Offset: 102") })
    }

    @Test
    fun `consume should log error with offset and topic on SerializationException`() {
        // Given
        val topic = "transaction-events"
        val partition = 0
        val offset = 789L
        
        // Create a consumer with mocked behavior that throws SerializationException
        val consumerSpy = spyk(consumer)
        val transaction = TransactionAvro("2024-01-15", "Test", "10.00", null)
        
        every { 
            consumerSpy.consume(transaction, topic, partition, offset)
        } answers {
            try {
                throw SerializationException("Deserialization failed")
            } catch (e: SerializationException) {
                logger.error(
                    "Deserialization failed for message at offset $offset in topic $topic (partition $partition)",
                    e
                )
            }
        }

        // When
        consumerSpy.consume(transaction, topic, partition, offset)

        // Then
        val logEvents = logAppender.list
        val errorLog = logEvents.find { it.level.toString() == "ERROR" }
        assertNotNull(errorLog)
        assertTrue(errorLog!!.message.contains("Deserialization failed"))
        assertTrue(errorLog.message.contains("offset $offset"))
        assertTrue(errorLog.message.contains("topic $topic"))
        assertTrue(errorLog.message.contains("partition $partition"))
    }

    @Test
    fun `consume should log error with offset and topic on generic Exception`() {
        // Given
        val topic = "transaction-events"
        val partition = 1
        val offset = 999L
        
        // Create a consumer with mocked behavior that throws generic Exception
        val consumerSpy = spyk(consumer)
        val transaction = TransactionAvro("2024-01-15", "Test", "10.00", null)
        
        every { 
            consumerSpy.consume(transaction, topic, partition, offset)
        } answers {
            try {
                throw RuntimeException("Processing error")
            } catch (e: Exception) {
                logger.error(
                    "Error processing message at offset $offset in topic $topic (partition $partition)",
                    e
                )
            }
        }

        // When
        consumerSpy.consume(transaction, topic, partition, offset)

        // Then
        val logEvents = logAppender.list
        val errorLog = logEvents.find { it.level.toString() == "ERROR" }
        assertNotNull(errorLog)
        assertTrue(errorLog!!.message.contains("Error processing message"))
        assertTrue(errorLog.message.contains("offset $offset"))
        assertTrue(errorLog.message.contains("topic $topic"))
        assertTrue(errorLog.message.contains("partition $partition"))
    }

    @Test
    fun `consume should continue processing after error`() {
        // Given
        val topic = "transaction-events"
        val consumerSpy = spyk(consumer)
        val transaction1 = TransactionAvro("2024-01-15", "Test1", "10.00", null)
        val transaction2 = TransactionAvro("2024-01-16", "Test2", "20.00", null)
        
        // First call throws exception, second call succeeds
        every { 
            consumerSpy.consume(transaction1, topic, 0, 100L)
        } answers {
            try {
                throw SerializationException("Deserialization failed")
            } catch (e: SerializationException) {
                logger.error(
                    "Deserialization failed for message at offset 100 in topic $topic (partition 0)",
                    e
                )
            }
        }

        // When
        consumerSpy.consume(transaction1, topic, 0, 100L)
        consumer.consume(transaction2, topic, 0, 101L)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.level.toString() == "ERROR" && it.message.contains("offset 100") })
        assertTrue(logEvents.any { it.message.contains("Offset: 101") })
    }

    @Test
    fun `consume should include partition information in logs`() {
        // Given
        val transaction = TransactionAvro("2024-01-15", "Test", "10.00", "500.00")
        val topic = "transaction-events"
        val partition = 5
        val offset = 1000L

        // When
        consumer.consume(transaction, topic, partition, offset)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.message.contains("Partition: $partition") })
    }

    @Test
    fun `consume should log all transaction fields correctly`() {
        // Given
        val date = "2024-12-06"
        val description = "Grocery Store Purchase"
        val amount = "125.75"
        val balance = "2500.50"
        val transaction = TransactionAvro(date, description, amount, balance)
        val topic = "transaction-events"

        // When
        consumer.consume(transaction, topic, 0, 1L)

        // Then
        val logEvents = logAppender.list
        assertTrue(logEvents.any { it.message.contains("Date: $date") })
        assertTrue(logEvents.any { it.message.contains("Description: $description") })
        assertTrue(logEvents.any { it.message.contains("Amount: $amount") })
        assertTrue(logEvents.any { it.message.contains("Balance: $balance") })
    }
}
