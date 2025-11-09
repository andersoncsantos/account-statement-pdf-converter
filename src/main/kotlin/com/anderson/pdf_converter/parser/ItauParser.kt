package com.anderson.pdf_converter.parser

import com.anderson.pdf_converter.model.Transaction
import com.anderson.pdf_converter.util.MonetaryValueConverter
import com.anderson.pdf_converter.util.PdfTextExtractor
import java.io.InputStream
import org.springframework.stereotype.Component

@Component
class ItauParser : PdfStatementParser {

    override fun canParse(text: String): Boolean {
        // heurística simples: extratos Itaú costumam conter a palavra 'EXTRATO DE CONTA'
        return text.contains("EXTRATO DE CONTA", ignoreCase = true) || text.contains("EXTRATO", ignoreCase = true)
    }

    override fun parse(inputStream: InputStream): List<Transaction> {
        val text = PdfTextExtractor.extractText(inputStream)

        val linhas = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { Regex("""\d{2}/\d{2}/\d{4}""").containsMatchIn(it) }
            .filterNot { it.contains("SALDO TOTAL", ignoreCase = true) }
            .filterNot { it.contains("SALDO ANTERIOR", ignoreCase = true) }

        val transactions = mutableListOf<Transaction>()
        val valorRegex = Regex("""-?\d{1,3}(?:\.\d{3})*,\d{2}""")

        for (linha in linhas) {
            val partes = linha.split(" ").filter { it.isNotBlank() }
            val data = partes.first()
            val valorRaw = partes.find { valorRegex.matches(it) } ?: ""
            val valor = MonetaryValueConverter.convertBrazilianFormat(valorRaw)
            val lancamento = partes.drop(1).filter { it != valorRaw }.joinToString(" ")
            transactions.add(Transaction(data, lancamento, valor))
        }

        return transactions
    }


}
