package com.anderson.pdf_converter.util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.InputStream

object PdfTextExtractor {
    fun extractText(inputStream: InputStream, startPage: Int = 1, endPage: Int? = null): String {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        stripper.startPage = startPage
        stripper.endPage = endPage ?: document.numberOfPages
        val text = stripper.getText(document)
        document.close()
        return text
    }
}
