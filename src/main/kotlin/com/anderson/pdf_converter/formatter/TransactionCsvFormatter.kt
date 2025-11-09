package com.anderson.pdf_converter.formatter

import com.anderson.pdf_converter.model.Transaction
import com.anderson.pdf_converter.util.CsvEscapeUtil
import org.springframework.stereotype.Component

@Component
class TransactionCsvFormatter : CsvFormatter {
    override fun format(transactions: List<Transaction>): String {
        val csv = StringBuilder("Data,Descrição,Valor (R$)\n")
        for (t in transactions) {
            csv.append("${t.date},${CsvEscapeUtil.escape(t.description)},${t.amount}\n")
        }
        return csv.toString()
    }
}
