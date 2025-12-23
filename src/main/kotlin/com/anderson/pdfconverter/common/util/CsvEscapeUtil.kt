package com.anderson.pdfconverter.common.util

object CsvEscapeUtil {
    fun escape(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) "\"${value.replace("\"", "\"\"")}\"" else value
    }
}
