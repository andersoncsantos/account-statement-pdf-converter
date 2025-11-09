package com.anderson.pdf_converter.parser

import com.anderson.pdf_converter.model.Transaction
import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component

@Component
class MercadoPagoParser : PdfStatementParser {

    override fun canParse(text: String): Boolean {
        return text.contains("DETALHE DOS MOVIMENTOS", ignoreCase = true)
    }

    override fun parse(inputStream: InputStream): List<Transaction> {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val totalPages = document.numberOfPages

        // Extrai todo o texto
        stripper.startPage = 1
        stripper.endPage = totalPages
        val text = stripper.getText(document)
        document.close()

        // Localiza seção
        val detailStart = text.indexOf("DETALHE DOS MOVIMENTOS")
        val detailEnd = text.length
        val detailSection = if (detailStart >= 0) text.substring(detailStart, detailEnd) else text

        // Preprocess
        var content = detailSection
        val pageMarkerRegex = Regex("(?m)\\b\\d{1,2}/\\d{1,2}\\b(?!/\\d)")
        content = pageMarkerRegex.replace(content, "\n")
        val saldoFinalInline = Regex("Saldo final:\\s*R\\$\\s*[\\d.,-]+", RegexOption.IGNORE_CASE)
        content = saldoFinalInline.replace(content, "\n")
        content = content.replace(Regex("[ \\t]{2,}"), " ")
        content = content.replace(Regex("\\n{2,}"), "\n")

        // Extrai transações usando varredura por datas
        // Aceita possíveis espaços em branco antes da data no início da linha
        val txDateLinePattern = Regex("(?m)^\\s*(\\d{2}[-/]\\d{2}[-/]\\d{4})")
        val idValuePattern = Regex("(\\d{6,})\\s+R\\$\\s*(-?[\\d.,]+)(?:\\s+R\\$\\s*-?[\\d.,]+)?")

        val transactions = mutableListOf<Transaction>()
        val dateMatches = txDateLinePattern.findAll(content).toList()
        if (dateMatches.isNotEmpty()) {
            for ((idx, dm) in dateMatches.withIndex()) {
                // usa o grupo 1 para obter a data sem espaços à esquerda
                val data = dm.groupValues[1]
                val searchStart = dm.range.last + 1
                val nextDateStart = if (idx + 1 < dateMatches.size) dateMatches[idx + 1].range.first else content.length

                val idMatch = idValuePattern.find(content, searchStart)
                if (idMatch != null && idMatch.range.first < nextDateStart) {
                    val valor = idMatch.groupValues[2].replace(".", "").replace(",", ".")
                    val descricaoRaw = content.substring(searchStart, idMatch.range.first)
                    val descricao = descricaoRaw.replace(Regex("\\s+"), " ").trim()
                    transactions.add(Transaction(data, descricao, valor))
                } else {
                    val segment = content.substring(searchStart, nextDateStart)
                    var match = idValuePattern.find(segment)
                    if (match == null) {
                        val idOnlyPattern = Regex("\\b(\\d{6,})\\b")
                        val amountPattern = Regex("R\\$\\s*-?[\\d.,]+")
                        val idOnly = idOnlyPattern.find(segment)
                        val amountOnly = amountPattern.find(segment)
                        if (idOnly != null && amountOnly != null) {
                            match = idValuePattern.find("${'$'}{idOnly.value} ${'$'}{amountOnly.value}")
                        }
                    }
                    if (match != null) {
                        val valor = match.groupValues[2].replace(".", "").replace(",", ".")
                        val descricaoRaw = segment.substring(0, match.range.first)
                        val descricao = descricaoRaw.replace(Regex("\\s+"), " ").trim()
                        transactions.add(Transaction(data, descricao, valor))
                    }
                }
            }
        }

        return transactions
    }

}
