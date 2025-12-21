package com.anderson.pdf_converter.parser


import com.anderson.pdf_converter.domain.model.Transaction
import com.anderson.pdf_converter.util.MonetaryValueConverter
import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component

@Component
class NubankParser : PdfStatementParser {

    override fun canParse(text: String): Boolean {
        // A verificação para identificar o tipo de extrato permanece a mesma.
        return text.contains("Movimentações", ignoreCase = true) &&
                text.contains("nubank.com.br", ignoreCase = true)
    }

    override fun parse(inputStream: InputStream): List<Transaction> {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper().apply { sortByPosition = true }
        val text = stripper.getText(document)
        document.close()

        val transactions = mutableListOf<Transaction>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        var currentDate = ""
        var i = 0

        // Regex para encontrar a data no formato "DD MÊS AAAA" (ex: "04 AGO 2025")
        val datePattern = Regex("""^(\d{2}\s+[A-Z]{3}\s+\d{4})""")
        // Regex para extrair um valor monetário no final de uma linha
        val valueAtEndRegex = Regex("""\s([\d.,]+)$""")
        // Regex para identificar uma linha que contém *apenas* um valor monetário
        val valueOnOwnLineRegex = Regex("""^[\d.,]+$""")
        // Palavras-chave que indicam o início de uma nova transação
        val transactionKeywords = listOf("Transferência", "Compra no débito", "Pagamento de fatura")

        while (i < lines.size) {
            val line = lines[i]

            // 1. Encontra e define a data atual para o contexto das transações seguintes
            val dateMatch = datePattern.find(line)
            if (dateMatch != null) {
                currentDate = dateMatch.groupValues[1]
                i++
                continue
            }

            // 2. Verifica se a linha atual é o início de uma transação, usando as palavras-chave
            if (currentDate.isNotEmpty() && transactionKeywords.any { line.startsWith(it, ignoreCase = true) }) {

                // CASO A: Transação de linha única (descrição e valor na mesma linha)
                val valueMatch = valueAtEndRegex.find(line)
                if (valueMatch != null) {
                    val value = MonetaryValueConverter.convertBrazilianFormat(valueMatch.groupValues[1])
                    val description = line.take(valueMatch.range.first).trim()
                    transactions.add(Transaction(currentDate, description, value))
                    i++ // Avança para a próxima linha
                    continue
                }

                // CASO B: Transação de múltiplas linhas
                val descriptionParts = mutableListOf(line)
                var j = i + 1
                var transactionFound = false
                while (j < lines.size) {
                    val nextLine = lines[j]

                    // Condição de parada: encontrou uma nova data ou um resumo ("Total de...")
                    if (datePattern.find(nextLine) != null || nextLine.startsWith("Total de")) {
                        break
                    }

                    // Condição de sucesso: encontrou a linha que contém apenas o valor
                    if (valueOnOwnLineRegex.matches(nextLine)) {
                        val description = descriptionParts.joinToString(" ")
                        val value = MonetaryValueConverter.convertBrazilianFormat(nextLine)
                        transactions.add(Transaction(currentDate, description, value))

                        i = j + 1 // Atualiza o contador principal para depois da transação
                        transactionFound = true
                        break
                    }

                    // Se não, a linha é parte da descrição
                    descriptionParts.add(nextLine)
                    j++
                }

                if (!transactionFound) {
                    // Se a transação não foi finalizada, avança uma linha para evitar loop infinito
                    i++
                }
            } else {
                // Se a linha não é uma data nem o início de uma transação, apenas avança
                i++
            }
        }
        return transactions
    }


}
