package com.anderson.pdf_converter.parser

import java.io.InputStream
import com.anderson.pdf_converter.model.Transaction

interface PdfStatementParser {
    fun canParse(text: String): Boolean
    fun parse(inputStream: InputStream): List<Transaction>
}
