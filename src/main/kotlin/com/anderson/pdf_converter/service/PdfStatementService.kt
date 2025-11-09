package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.formatter.CsvFormatter
import com.anderson.pdf_converter.parser.PdfStatementParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class PdfStatementService(
    private val parsers: List<PdfStatementParser>,
    private val detector: ParserDetectorService,
    private val csvFormatter: CsvFormatter
) {

    private val logger = LoggerFactory.getLogger(PdfStatementService::class.java)

    fun convertPdfToCsv(inputStream: InputStream): String {
        val bytes = inputStream.readAllBytes()

        val parser = detector.detect(bytes)
            ?: throw IllegalArgumentException("Nenhum parser disponível para este PDF")

        logger.debug("Parser selecionado: ${parser::class.simpleName}")

        val transactions = parser.parse(bytes.inputStream())
        return csvFormatter.format(transactions)
    }
}
