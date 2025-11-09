package com.anderson.pdf_converter.service

import com.anderson.pdf_converter.parser.PdfStatementParser
import com.anderson.pdf_converter.util.PdfTextExtractor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class ParserDetectorService(
    private val parsers: List<PdfStatementParser>
) {

    private val logger = LoggerFactory.getLogger(ParserDetectorService::class.java)

    fun detect(bytes: ByteArray): PdfStatementParser? {
        val fullText = extractFullText(bytes)
        logger.debug("Texto completo (trecho) para detecção: ${fullText.take(400)}")

        // Heurística: Nubank
        try {
            if (fullText.contains("Movimentações", ignoreCase = true) && 
                fullText.contains("nubank.com.br", ignoreCase = true)) {
                val nubank = parsers.find { p ->
                    val name = p::class.simpleName ?: ""
                    name.contains("Nubank", ignoreCase = true) && try { p.canParse(fullText) } catch (e: Exception) { false }
                }
                if (nubank != null) {
                    logger.info("Detector: selecionando NubankParser por marcador presente")
                    return nubank
                }
            }
        } catch (ex: Exception) {
            logger.warn("Erro ao aplicar heurística Nubank: ${ex.message}")
        }

        // Heurística: Mercado Pago
        try {
            if (fullText.contains("DETALHE DOS MOVIMENTOS", ignoreCase = true)) {
                val mercado = parsers.find { p ->
                    val name = p::class.simpleName ?: ""
                    name.contains("Mercado", ignoreCase = true) && try { p.canParse(fullText) } catch (e: Exception) { false }
                }
                if (mercado != null) {
                    logger.info("Detector: selecionando MercadoPagoParser por marcador presente")
                    return mercado
                }
            }
        } catch (ex: Exception) {
            logger.warn("Erro ao aplicar heurística MercadoPago: ${ex.message}")
        }

        // Fallback: procura o primeiro parser cuja canParse retorna true
        return parsers.find { parser ->
            try {
                parser.canParse(fullText)
            } catch (ex: Exception) {
                logger.warn("Parser ${parser::class.java.simpleName} threw during canParse: ${ex.message}")
                false
            }
        }
    }

    private fun extractFullText(bytes: ByteArray): String {
        return PdfTextExtractor.extractText(ByteArrayInputStream(bytes))
    }
}
