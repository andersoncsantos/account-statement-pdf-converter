package com.anderson.pdf_converter.controller

import com.anderson.pdf_converter.service.PdfProcessingService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
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
    private val pdfProcessingService: PdfProcessingService
) {

    private val logger = LoggerFactory.getLogger(PdfController::class.java)

    @PostMapping("/convert", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun convertPdf(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val bytes = file.inputStream.readAllBytes()
        val csv = pdfProcessingService.processPdf(bytes)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }
}
