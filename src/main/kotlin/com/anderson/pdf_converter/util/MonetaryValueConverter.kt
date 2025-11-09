package com.anderson.pdf_converter.util

object MonetaryValueConverter {
    fun convertBrazilianFormat(value: String): String {
        return value.replace(".", "").replace(",", ".")
    }
}
