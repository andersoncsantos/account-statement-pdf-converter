package com.anderson.pdfconverter.adapters.http

import com.anderson.pdfconverter.domain.ports.`in`.CsvFormatter
import com.anderson.pdfconverter.application.service.parser.PdfStatementParser
import com.anderson.pdfconverter.application.service.ParserDetectorService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/pdf")
class PdfController(
    private val detector: ParserDetectorService,
    private val csvFormatter: CsvFormatter
) {

    private val logger = LoggerFactory.getLogger(PdfController::class.java)

    @PostMapping("/convert", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun convertPdf(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        return try {
            // Lê todo o arquivo em memória e detecta o parser pela primeira página
            val bytes = file.inputStream.readAllBytes()
            val parser: PdfStatementParser? = detector.detect(bytes)
            if (parser == null) {
                logger.warn("Parser não detectado para o PDF recebido")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Não foi possível detectar automaticamente o parser para esse PDF")
            }
            logger.info("Parser detectado: ${parser::class.java.simpleName}")

            val transactions = parser.parse(bytes.inputStream())
            val csv = csvFormatter.format(transactions)

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv)
        } catch (ex: Exception) {
            logger.error("Erro ao processar PDF", ex)
            val message = "Erro ao processar PDF: ${ex.message}"
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message)
        }
    }
}