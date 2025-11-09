package com.anderson.pdf_converter.parser

import com.anderson.pdf_converter.model.Transaction
import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component

@Component
class ItauParser : PdfStatementParser {

    override fun canParse(text: String): Boolean {
        // heurística simples: extratos Itaú costumam conter a palavra 'EXTRATO DE CONTA'
        return text.contains("EXTRATO DE CONTA", ignoreCase = true) || text.contains("EXTRATO", ignoreCase = true)
    }

    override fun parse(inputStream: InputStream): List<Transaction> {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()

        val linhas = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { Regex("""\d{2}/\d{2}/\d{4}""").containsMatchIn(it) }
            .filterNot { it.contains("SALDO TOTAL", ignoreCase = true) }
            .filterNot { it.contains("SALDO ANTERIOR", ignoreCase = true) }

        val transactions = mutableListOf<Transaction>()
        val valorRegex = Regex("""-?\d+[.,]\d{2}""")

        for (linha in linhas) {
            val partes = linha.split(" ").filter { it.isNotBlank() }
            val data = partes.first()
            val valor = partes.find { valorRegex.matches(it) } ?: ""
            val lancamento = partes.drop(1).filter { it != valor }.joinToString(" ")
            transactions.add(Transaction(data, lancamento, valor))
        }

        return transactions
    }

    override fun formatCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder("Data,Descrição,Valor (R$)\n")
        for (t in transactions) {
            sb.append("${t.date},${csvSafe(t.description)},${t.amount}\n")
        }
        return sb.toString()
    }

    private fun csvSafe(s: String): String {
        val needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n")
        return if (needsQuotes) "\"${s.replace("\"", "\"\"")}\"" else s
    }
}
