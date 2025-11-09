package com.anderson.pdf_converter

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayOutputStream

@SpringBootTest
@AutoConfigureMockMvc
class ControllerIntegrationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun makePdfBytes(text: String): ByteArray {
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
                cs.endText()
                y -= 12f
                if (line !== lines.last()) {
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, 10f)
                }
            }
        }
        val baos = ByteArrayOutputStream()
        doc.save(baos)
        doc.close()
        return baos.toByteArray()
    }

    @Test
    fun `should convert MercadoPago PDF`() {
        val content = "EXTRATO DE CONTA\nSome header\nDETALHE DOS MOVIMENTOS\n09/07/2025\nTransferência Pix enviada Anderson Correa dos Santos\n117477181939 R$ -0,01 R$ 99,99"
        val pdfBytes = makePdfBytes(content)

        mockMvc.perform(
            multipart("/api/pdf/convert").file("file", pdfBytes)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should convert Itau PDF`() {
        val content = "EXTRATO DE CONTA\n21/07/2025 Pagamento 1731586274201 R$ 0,46"
        val pdfBytes = makePdfBytes(content)

        mockMvc.perform(
            multipart("/api/pdf/convert").file("file", pdfBytes)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should convert Nubank PDF`() {
        val content = "Movimentações\nnubank.com.br\n04 AGO 2025\nTransferência enviada\n100,00"
        val pdfBytes = makePdfBytes(content)

        mockMvc.perform(
            multipart("/api/pdf/convert").file("file", pdfBytes)
        )
            .andExpect(status().isOk)
    }
}
