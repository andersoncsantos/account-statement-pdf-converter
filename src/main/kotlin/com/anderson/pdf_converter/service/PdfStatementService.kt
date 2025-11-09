package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.model.Transaction
import com.anderson.pdf_converter.parser.PdfStatementParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class PdfStatementService(
    private val parsers: List<PdfStatementParser>,
    private val detector: ParserDetectorService
) {

    private val logger = LoggerFactory.getLogger(PdfStatementService::class.java)

    fun convertPdfToCsv(inputStream: InputStream): String {
        val bytes = inputStream.readAllBytes()

        val parser = detector.detect(bytes)
            ?: throw IllegalArgumentException("Nenhum parser disponível para este PDF")

        logger.debug("Parser selecionado: ${parser::class.simpleName}")

        val transactions = parser.parse(bytes.inputStream())

        return parser.formatCsv(transactions)
    }

    private fun extractFirstPageText(bytes: ByteArray): String {
        PDDocument.load(bytes.inputStream()).use { document ->
            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = 1
            return stripper.getText(document)
        }
    }

    private fun defaultCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder("Data,Descrição,Valor (R$)\n")
        for (t in transactions) {
            sb.append("${t.date},${csvSafe(t.description)},${t.amount}\n")
        }
        return sb.toString()
    }

    private fun csvSafe(s: String): String {
        val needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n")
        return if (needsQuotes) "\"${s.replace("\"", "\"\"")}\"" else s
    }
}
