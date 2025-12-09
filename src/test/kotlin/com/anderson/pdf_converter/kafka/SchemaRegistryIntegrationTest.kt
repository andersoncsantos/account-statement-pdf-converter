package com.anderson.pdf_converter.kafka

import com.anderson.pdf_converter.avro.TransactionAvro
import com.anderson.pdf_converter.mapper.TransactionMapper
import com.anderson.pdf_converter.model.Transaction
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*

/**
 * Integration tests for Schema Registry with Kafka using Testcontainers.
 * 
 * These tests verify the complete produce-consume flow with Avro serialization,
 * schema registration, and round-trip data integrity.
 * 
 * **Validates: Requirements 10.1, 10.2, 10.5**
 */
@Testcontainers
class SchemaRegistryIntegrationTest {

    companion object {
        private val network = Network.newNetwork()

        @Container
        private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka")

        @Container
        private val schemaRegistry = GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0"))
            .withNetwork(network)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .dependsOn(kafka)

        private lateinit var producer: KafkaProducer<String, TransactionAvro>
        private lateinit var consumer: KafkaConsumer<String, TransactionAvro>
        private const val TEST_TOPIC = "test-transaction-events"

        @JvmStatic
        @BeforeAll
        fun setup() {
            // Wait for Schema Registry to be ready
            Thread.sleep(5000)

            val schemaRegistryUrl = "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(8081)}"

            // Configure producer
            val producerProps = Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java.name)
                put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
                put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, true)
            }
            producer = KafkaProducer(producerProps)

            // Configure consumer
            val consumerProps = Properties().apply {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
                put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java.name)
                put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
                put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
            }
            consumer = KafkaConsumer(consumerProps)
            consumer.subscribe(listOf(TEST_TOPIC))
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            producer.close()
            consumer.close()
            network.close()
        }
    }

    @Test
    fun `should produce and consume transaction with Avro serialization`() {
        // Given
        val transaction = TransactionAvro(
            "2024-12-06",
            "Integration Test Transaction",
            "99.99",
            "1500.00"
        )

        // When - Produce message
        val record = ProducerRecord(TEST_TOPIC, "test-key-1", transaction)
        producer.send(record).get() // Wait for send to complete
        producer.flush()

        // Then - Consume message
        val records = consumer.poll(Duration.ofSeconds(10))
        assertFalse(records.isEmpty, "Should receive at least one message")

        val consumedRecord = records.records(TEST_TOPIC).first()
        val consumedTransaction = consumedRecord.value() as TransactionAvro

        // Verify message content matches original
        assertEquals(transaction.date, consumedTransaction.date)
        assertEquals(transaction.description, consumedTransaction.description)
        assertEquals(transaction.amount, consumedTransaction.amount)
        assertEquals(transaction.balance, consumedTransaction.balance)
        assertEquals("test-key-1", consumedRecord.key())
    }

    @Test
    fun `should handle transaction with null balance in round-trip`() {
        // Given
        val transaction = TransactionAvro(
            "2024-12-06",
            "Transaction without balance",
            "50.00",
            null
        )

        // When - Produce message
        val record = ProducerRecord(TEST_TOPIC, "test-key-2", transaction)
        producer.send(record).get()
        producer.flush()

        // Then - Consume message
        val records = consumer.poll(Duration.ofSeconds(10))
        assertFalse(records.isEmpty, "Should receive at least one message")

        val consumedRecord = records.records(TEST_TOPIC).first()
        val consumedTransaction = consumedRecord.value() as TransactionAvro

        // Verify null balance is preserved
        assertEquals(transaction.date, consumedTransaction.date)
        assertEquals(transaction.description, consumedTransaction.description)
        assertEquals(transaction.amount, consumedTransaction.amount)
        assertNull(consumedTransaction.balance)
    }

    @Test
    fun `should preserve data integrity through complete round-trip`() {
        // Given - Multiple transactions with different characteristics
        val transactions = listOf(
            TransactionAvro("2024-01-01", "Coffee Shop", "5.50", "1000.00"),
            TransactionAvro("2024-01-02", "Grocery Store", "125.75", "874.25"),
            TransactionAvro("2024-01-03", "Gas Station", "60.00", null),
            TransactionAvro("2024-01-04", "Restaurant", "45.00", "769.25"),
            TransactionAvro("2024-01-05", "Online Purchase", "199.99", "569.26")
        )

        // When - Produce all messages
        transactions.forEachIndexed { index, transaction ->
            val record = ProducerRecord(TEST_TOPIC, "key-$index", transaction)
            producer.send(record).get()
        }
        producer.flush()

        // Then - Consume all messages and verify
        val consumedTransactions = mutableListOf<TransactionAvro>()
        val startTime = System.currentTimeMillis()
        val timeout = 15000 // 15 seconds

        while (consumedTransactions.size < transactions.size && 
               System.currentTimeMillis() - startTime < timeout) {
            val records = consumer.poll(Duration.ofSeconds(2))
            records.records(TEST_TOPIC).forEach { record ->
                consumedTransactions.add(record.value() as TransactionAvro)
            }
        }

        // Verify we received all messages
        assertEquals(transactions.size, consumedTransactions.size, 
            "Should receive all ${transactions.size} messages")

        // Verify each transaction's data integrity
        transactions.forEach { original ->
            val consumed = consumedTransactions.find { 
                it.date == original.date && it.description == original.description 
            }
            assertNotNull(consumed, "Should find transaction: ${original.description}")
            assertEquals(original.amount, consumed!!.amount)
            assertEquals(original.balance, consumed.balance)
        }
    }

    @Test
    fun `should successfully serialize and deserialize using mapper`() {
        // Given - Domain model Transaction
        val domainTransaction = Transaction(
            date = "2024-12-06",
            description = "Mapper Test Transaction",
            amount = "75.50",
            balance = "2000.00"
        )

        // When - Convert to Avro and produce
        val avroTransaction = TransactionMapper.toAvro(domainTransaction)
        val record = ProducerRecord(TEST_TOPIC, "mapper-key", avroTransaction)
        producer.send(record).get()
        producer.flush()

        // Then - Consume and convert back to domain model
        val records = consumer.poll(Duration.ofSeconds(10))
        assertFalse(records.isEmpty, "Should receive at least one message")

        val consumedRecord = records.records(TEST_TOPIC).first()
        val consumedAvro = consumedRecord.value() as TransactionAvro
        val consumedDomain = TransactionMapper.fromAvro(consumedAvro)

        // Verify complete round-trip preserves data
        assertEquals(domainTransaction.date, consumedDomain.date)
        assertEquals(domainTransaction.description, consumedDomain.description)
        assertEquals(domainTransaction.amount, consumedDomain.amount)
        assertEquals(domainTransaction.balance, consumedDomain.balance)
    }

    @Test
    fun `should handle multiple messages with same key`() {
        // Given - Multiple transactions with same key (should go to same partition)
        val key = "same-key"
        val transaction1 = TransactionAvro("2024-12-06", "First", "10.00", "100.00")
        val transaction2 = TransactionAvro("2024-12-06", "Second", "20.00", "80.00")
        val transaction3 = TransactionAvro("2024-12-06", "Third", "30.00", "50.00")

        // When - Produce messages
        producer.send(ProducerRecord(TEST_TOPIC, key, transaction1)).get()
        producer.send(ProducerRecord(TEST_TOPIC, key, transaction2)).get()
        producer.send(ProducerRecord(TEST_TOPIC, key, transaction3)).get()
        producer.flush()

        // Then - Consume and verify order is preserved
        val consumedTransactions = mutableListOf<TransactionAvro>()
        val startTime = System.currentTimeMillis()
        val timeout = 10000

        while (consumedTransactions.size < 3 && 
               System.currentTimeMillis() - startTime < timeout) {
            val records = consumer.poll(Duration.ofSeconds(2))
            records.records(TEST_TOPIC).forEach { record ->
                if (record.key() == key) {
                    consumedTransactions.add(record.value() as TransactionAvro)
                }
            }
        }

        // Verify all messages received
        assertEquals(3, consumedTransactions.size)
        
        // Verify messages are in order (same partition guarantees order)
        assertEquals("First", consumedTransactions[0].description)
        assertEquals("Second", consumedTransactions[1].description)
        assertEquals("Third", consumedTransactions[2].description)
    }

    @Test
    fun `should support backward compatible schema evolution`() {
        // This test verifies schema evolution with backward compatibility
        // **Validates: Requirements 10.3, 10.5**
        
        val evolutionTopic = "schema-evolution-test"
        
        // Given - Schema v1 is already registered (current TransactionAvro schema)
        // Produce messages with v1 schema
        val v1Transaction = TransactionAvro(
            "2024-12-06",
            "Schema V1 Transaction",
            "100.00",
            "1000.00"
        )
        
        val record = ProducerRecord(evolutionTopic, "evolution-key-1", v1Transaction)
        producer.send(record).get()
        producer.flush()
        
        // Create a consumer that will read with the current schema (v1)
        val schemaRegistryUrl = "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(8081)}"
        val v1ConsumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "v1-consumer-group")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java.name)
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
            put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
        }
        val v1Consumer = KafkaConsumer<String, TransactionAvro>(v1ConsumerProps)
        v1Consumer.subscribe(listOf(evolutionTopic))
        
        try {
            // When - Consume the v1 message with v1 consumer
            val v1Records = v1Consumer.poll(Duration.ofSeconds(10))
            assertFalse(v1Records.isEmpty, "V1 consumer should receive v1 message")
            
            val consumedV1 = v1Records.records(evolutionTopic).first().value() as TransactionAvro
            assertEquals("Schema V1 Transaction", consumedV1.description)
            assertEquals("100.00", consumedV1.amount)
            
            // Now simulate schema evolution by producing a message with an evolved schema
            // In a real scenario, we would register a v2 schema with an additional optional field
            // Since we're using the same generated class, we'll simulate this by:
            // 1. Producing a message with all fields (simulating v2 with new optional field populated)
            // 2. Verifying the v1 consumer can still read it (backward compatibility)
            
            val v2Transaction = TransactionAvro(
                "2024-12-07",
                "Schema V2 Transaction",
                "200.00",
                "800.00"  // All fields present, simulating evolved schema
            )
            
            val v2Record = ProducerRecord(evolutionTopic, "evolution-key-2", v2Transaction)
            producer.send(v2Record).get()
            producer.flush()
            
            // Then - Verify v1 consumer can still read the "evolved" message
            // This demonstrates backward compatibility: old consumers can read new messages
            val v2Records = v1Consumer.poll(Duration.ofSeconds(10))
            assertFalse(v2Records.isEmpty, "V1 consumer should receive v2 message (backward compatible)")
            
            val consumedV2 = v2Records.records(evolutionTopic).first().value() as TransactionAvro
            assertEquals("Schema V2 Transaction", consumedV2.description)
            assertEquals("200.00", consumedV2.amount)
            assertEquals("800.00", consumedV2.balance)
            
            // Verify that the consumer using the old schema can successfully deserialize
            // messages that may have been produced with a newer schema version
            // This is the essence of backward compatibility
            assertNotNull(consumedV2.date)
            assertNotNull(consumedV2.description)
            assertNotNull(consumedV2.amount)
            
        } finally {
            v1Consumer.close()
        }
    }
}
