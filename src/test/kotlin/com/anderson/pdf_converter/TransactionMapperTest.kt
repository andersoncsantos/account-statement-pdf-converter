package com.anderson.pdf_converter

import com.anderson.pdfconverter.domain.model.Transaction
import com.anderson.pdfconverter.adapters.avro.TransactionMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TransactionMapperTest {

    @Test
    fun `toAvro should convert Transaction to TransactionAvro`() {
        // Given
        val transaction = Transaction(
            date = "2024-01-15",
            description = "Coffee Shop",
            amount = "15.50",
            balance = "1000.00"
        )

        // When
        val avro = TransactionMapper.toAvro(transaction)

        // Then
        assertEquals("2024-01-15", avro.getDate())
        assertEquals("Coffee Shop", avro.getDescription())
        assertEquals("15.50", avro.getAmount())
        assertEquals("1000.00", avro.getBalance())
    }

    @Test
    fun `toAvro should handle null balance`() {
        // Given
        val transaction = Transaction(
            date = "2024-01-15",
            description = "Coffee Shop",
            amount = "15.50",
            balance = null
        )

        // When
        val avro = TransactionMapper.toAvro(transaction)

        // Then
        assertEquals("2024-01-15", avro.getDate())
        assertEquals("Coffee Shop", avro.getDescription())
        assertEquals("15.50", avro.getAmount())
        assertNull(avro.getBalance())
    }

    @Test
    fun `fromAvro should convert TransactionAvro to Transaction`() {
        // Given
        val avro = com.anderson.pdf_converter.avro.TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            "1000.00"
        )

        // When
        val transaction = TransactionMapper.fromAvro(avro)

        // Then
        assertEquals("2024-01-15", transaction.date)
        assertEquals("Coffee Shop", transaction.description)
        assertEquals("15.50", transaction.amount)
        assertEquals("1000.00", transaction.balance)
    }

    @Test
    fun `fromAvro should handle null balance`() {
        // Given
        val avro = com.anderson.pdf_converter.avro.TransactionAvro(
            "2024-01-15",
            "Coffee Shop",
            "15.50",
            null
        )

        // When
        val transaction = TransactionMapper.fromAvro(avro)

        // Then
        assertEquals("2024-01-15", transaction.date)
        assertEquals("Coffee Shop", transaction.description)
        assertEquals("15.50", transaction.amount)
        assertNull(transaction.balance)
    }

    @Test
    fun `round trip conversion should preserve data`() {
        // Given
        val original = Transaction(
            date = "2024-01-15",
            description = "Coffee Shop",
            amount = "15.50",
            balance = "1000.00"
        )

        // When
        val avro = TransactionMapper.toAvro(original)
        val result = TransactionMapper.fromAvro(avro)

        // Then
        assertEquals(original, result)
    }

    @Test
    fun `round trip conversion should preserve data with null balance`() {
        // Given
        val original = Transaction(
            date = "2024-01-15",
            description = "Coffee Shop",
            amount = "15.50",
            balance = null
        )

        // When
        val avro = TransactionMapper.toAvro(original)
        val result = TransactionMapper.fromAvro(avro)

        // Then
        assertEquals(original, result)
    }
}
