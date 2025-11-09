package com.anderson.pdf_converter

import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service

@Service
class PdfToCsvService {

    fun convertPdfToCsv(inputStream: InputStream): String {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()

        val linhas = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            // Mantém apenas linhas que começam com uma data (dd/MM/yyyy)
            .filter { Regex("""\d{2}/\d{2}/\d{4}""").containsMatchIn(it) }
            // Remove linhas de saldo diário e saldo anterior
            .filterNot { it.contains("SALDO TOTAL DISPONÍVEL DIA", ignoreCase = true) }
            .filterNot { it.contains("SALDO ANTERIOR", ignoreCase = true) }

        val csvBuilder = StringBuilder("Data,Lançamento,Valor (R$),Saldo (R$)\n")

        for (linha in linhas) {
            // Exemplo de parsing simples: separa por espaços
            val partes = linha.split(" ")
                .filter { it.isNotBlank() }

            val data = partes[0]
            val valorRegex = Regex("""-?\d+([.,]\d{2})""")
            val valor = partes.find { valorRegex.matches(it) } ?: ""

            // O lançamento é o restante da linha sem a data e o valor
            val lancamento = partes.drop(1).filter { it != valor }.joinToString(" ")

            csvBuilder.append("$data,$lancamento,$valor,\n")
        }

        return csvBuilder.toString()
    }
}

