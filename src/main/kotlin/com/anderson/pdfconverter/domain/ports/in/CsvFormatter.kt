package com.anderson.pdfconverter.domain.ports.`in`

import com.anderson.pdfconverter.domain.model.Transaction

interface CsvFormatter {
    fun format(transactions: List<Transaction>): String
}