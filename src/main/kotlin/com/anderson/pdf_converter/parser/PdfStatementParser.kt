package com.anderson.pdf_converter.parser

import com.anderson.pdf_converter.domain.model.Transaction
import java.io.InputStream


interface PdfStatementParser {
    fun canParse(text: String): Boolean
    fun parse(inputStream: InputStream): List<Transaction>
}
