package com.anderson.pdfconverter.common.util

object MonetaryValueConverter {
    fun convertBrazilianFormat(value: String): String {
        return value.replace(".", "").replace(",", ".")
    }
}
