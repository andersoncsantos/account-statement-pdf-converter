package com.anderson.pdfconverter.application.service

import com.anderson.pdfconverter.common.util.CsvEscapeUtil
import com.anderson.pdfconverter.domain.model.Transaction
import com.anderson.pdfconverter.domain.ports.`in`.CsvFormatter
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