package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.exception.ParserNotFoundException
import com.anderson.pdf_converter.model.Transaction
import com.anderson.pdf_converter.parser.PdfStatementParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PdfProcessingServiceTest {

    private lateinit var pdfProcessingService: PdfProcessingService
    private lateinit var detector: ParserDetectorService
    private lateinit var csvFormattingService: CsvFormattingService
    private lateinit var parser: PdfStatementParser

    @BeforeEach
    fun setup() {
        detector = mock()
        csvFormattingService = mock()
        parser = mock()
        pdfProcessingService = PdfProcessingService(detector, csvFormattingService)
    }

    @Test
    fun `should process PDF and return CSV`() {
        val bytes = "pdf content".toByteArray()
        val transactions = listOf(Transaction("2023-01-01", "Transaction 1", "100.00"))
        val expectedCsv = "csv content"

        whenever(detector.detect(bytes)).thenReturn(parser)
        whenever(parser.parse(any())).thenReturn(transactions)
        whenever(csvFormattingService.formatCsv(transactions)).thenReturn(expectedCsv)

        val actualCsv = pdfProcessingService.processPdf(bytes)

        assertEquals(expectedCsv, actualCsv)
    }

    @Test
    fun `should throw ParserNotFoundException when parser is not detected`() {
        val bytes = "pdf content".toByteArray()

        whenever(detector.detect(bytes)).thenReturn(null)

        assertThrows<ParserNotFoundException> {
            pdfProcessingService.processPdf(bytes)
        }
    }
}
