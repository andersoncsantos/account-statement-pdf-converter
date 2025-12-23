package com.anderson.pdf_converter

import com.anderson.pdfconverter.application.service.parser.MercadoPagoParser
import com.anderson.pdfconverter.application.service.parser.ItauParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun makePdfWithText(text: String): ByteArray {
    val doc = PDDocument()
    val page = PDPage()
    doc.addPage(page)
    PDPageContentStream(doc, page).use { cs ->
        cs.beginText()
        cs.setFont(PDType1Font.HELVETICA, 10f)
        var y = 700f
        val lines = text.split('\n')
        for (line in lines) {
            cs.newLineAtOffset(10f, y)
            cs.showText(line)
            // move para próxima linha: reset offset by closing and beginning text again
            cs.endText()
            y -= 12f
            if (line !== lines.last()) {
                cs.beginText()
                cs.setFont(PDType1Font.HELVETICA, 10f)
            }
        }
        // ensure we are in a closed text state
        try { cs.endText() } catch (_: Exception) {}
    }
    val baos = ByteArrayOutputStream()
    doc.save(baos)
    doc.close()
    return baos.toByteArray()
}

class ParserUnitTests {

    @Test
    fun testMercadoPagoParser_canParseAndParse() {
        val parser = MercadoPagoParser()
        val content = "EXTRATO DE CONTA\nDETALHE DOS MOVIMENTOS\n09/07/2025\nTransferência Pix enviada Anderson Correa dos Santos\n117477181939 R$ -0,01 R$ 99,99"
        assertTrue(parser.canParse(content))

        val pdfBytes = makePdfWithText(content)
        val transactions = parser.parse(ByteArrayInputStream(pdfBytes))
        assertTrue(transactions.isNotEmpty(), "MercadoPagoParser should extract at least one transaction")
        val t = transactions.first()
        assertEquals("09/07/2025", t.date)
        assertTrue(t.description.contains("Transferência"))
    }

    @Test
    fun testItauParser_canParseAndParse() {
        val parser = ItauParser()
        val content = "EXTRATO DE CONTA\n21/07/2025 Pagamento 1731586274201 R$ 0,46"
        assertTrue(parser.canParse(content))

        val pdfBytes = makePdfWithText(content)
        val transactions = parser.parse(ByteArrayInputStream(pdfBytes))
        assertTrue(transactions.isNotEmpty(), "ItauParser should extract at least one transaction")
        val t = transactions.first()
        assertEquals("21/07/2025", t.date)
        assertTrue(t.description.contains("Pagamento") || t.amount.isNotBlank())
    }
}
