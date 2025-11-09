package com.anderson.pdf_converter.formatter

import com.anderson.pdf_converter.model.Transaction

interface CsvFormatter {
    fun format(transactions: List<Transaction>): String
}
