
package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.exception.ParserNotFoundException
import com.anderson.pdf_converter.model.Transaction
import com.anderson.pdf_converter.parser.PdfStatementParser
import org.springframework.stereotype.Service

@Service
class PdfProcessingService(
    private val detector: ParserDetectorService,
    private val csvFormattingService: CsvFormattingService
) {
    fun processPdf(bytes: ByteArray): String {
        val parser: PdfStatementParser = detector.detect(bytes)
            ?: throw ParserNotFoundException("Não foi possível detectar o parser para o arquivo PDF.")
        val transactions: List<Transaction> = parser.parse(bytes.inputStream())
        return csvFormattingService.formatCsv(transactions)
    }
}
