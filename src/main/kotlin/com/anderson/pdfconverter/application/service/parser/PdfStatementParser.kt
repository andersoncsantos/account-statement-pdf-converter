package com.anderson.pdfconverter.application.service.parser

import com.anderson.pdfconverter.domain.model.Transaction
import java.io.InputStream


interface PdfStatementParser {
    fun canParse(text: String): Boolean
    fun parse(inputStream: InputStream): List<Transaction>
}
