package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.model.Transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvFormattingServiceTest {

    private val csvFormattingService = CsvFormattingService()

    @Test
    fun `should format transactions to CSV`() {
        val transactions = listOf(
            Transaction("2023-01-01", "Transaction 1", "100.00"),
            Transaction("2023-01-02", "Transaction, with comma", "200.00")
        )

        val expectedCsv = """
            Data,Lançamento,Valor (R$),Saldo (R$)
            2023-01-01,Transaction 1,100.00,
            2023-01-02,"Transaction, with comma",200.00,
        """.trimIndent() + "\n"

        val actualCsv = csvFormattingService.formatCsv(transactions)

        assertEquals(expectedCsv, actualCsv)
    }
}
