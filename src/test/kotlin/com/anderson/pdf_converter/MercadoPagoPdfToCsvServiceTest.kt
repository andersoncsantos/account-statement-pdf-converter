package com.anderson.pdf_converter

import java.io.File
import java.io.FileInputStream
import com.anderson.pdfconverter.application.service.PdfStatementService
import com.anderson.pdfconverter.application.service.ParserDetectorService
import com.anderson.pdfconverter.application.service.parser.MercadoPagoParser
import com.anderson.pdfconverter.application.service.parser.ItauParser
import com.anderson.pdfconverter.application.service.TransactionCsvFormatter
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MercadoPagoPdfToCsvServiceTest {

    @Test
    fun testConvertPdfToCsv_realPdf() {
        val path = "/home/anderson/Downloads/account_statement-dc27a042-1ade-406f-8878-2f370c5a7359.pdf"
        val file = File(path)
        Assumptions.assumeTrue(file.exists(), "Skipping test: MercadoPago PDF not found at $path")

        val mercado = MercadoPagoParser()
        val itau = ItauParser()
        val detector = ParserDetectorService(listOf(mercado, itau))
        val csvFormatter = TransactionCsvFormatter()
        val service = PdfStatementService(listOf(mercado, itau), detector, csvFormatter)

        FileInputStream(file).use { fis ->
            val csv = service.convertPdfToCsv(fis)
            // basic validations
            assertTrue(csv.isNotBlank(), "CSV should not be blank")
            val lines = csv.lines().filter { it.isNotBlank() }
            assertTrue(lines.size >= 2, "CSV should contain header+at least one record")
        }
    }

    @Test
    fun testItauParser_realPdf() {
        val path = "/home/anderson/Downloads/extrato-itau_04_11_2025_08-41.pdf"
        val file = File(path)
        Assumptions.assumeTrue(file.exists(), "Skipping test: Itaú PDF not found at $path")

        val mercado = MercadoPagoParser()
        val itau = ItauParser()
        val detector = ParserDetectorService(listOf(mercado, itau))
        val csvFormatter = TransactionCsvFormatter()
        val service = PdfStatementService(listOf(mercado, itau), detector, csvFormatter)

        FileInputStream(file).use { fis ->
            val csv = service.convertPdfToCsv(fis)
            // basic validations
            assertTrue(csv.isNotBlank(), "CSV should not be blank for Itaú PDF")
            val lines = csv.lines().filter { it.isNotBlank() }
            assertTrue(lines.size >= 2, "CSV should contain header+at least one record for Itaú PDF")
        }
    }
}
