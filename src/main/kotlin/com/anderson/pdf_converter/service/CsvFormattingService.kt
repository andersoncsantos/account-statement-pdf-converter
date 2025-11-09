package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.model.Transaction
import org.springframework.stereotype.Service

@Service
class CsvFormattingService {
    fun formatCsv(transactions: List<Transaction>): String {
        val csvBuilder = StringBuilder("Data,Lançamento,Valor (R$),Saldo (R$)\n")
        transactions.forEach {
            csvBuilder.append("${it.date},${escapeCsv(it.description)},${it.amount},\n")
        }
        return csvBuilder.toString()
    }

    private fun escapeCsv(s: String?): String {
        if (s == null) return ""
        val needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n")
        return if (needsQuotes) "\"${s.replace("\"", "\"\"")}\"" else s
    }
}
