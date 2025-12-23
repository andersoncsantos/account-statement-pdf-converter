package com.anderson.pdfconverter.application.service

import com.anderson.pdfconverter.application.service.parser.PdfStatementParser
import com.anderson.pdfconverter.common.util.PdfTextExtractor
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

        // Heurística: Itau
        try {
            if (fullText.contains("EXTRATO DE CONTA", ignoreCase = true)) {
                val itau = parsers.find { p ->
                    val name = p::class.simpleName ?: ""
                    name.contains("Itau", ignoreCase = true) && try { p.canParse(fullText) } catch (e: Exception) { false }
                }
                if (itau != null) {
                    logger.info("Detector: selecionando ItauParser por marcador presente")
                    return itau
                }
            }
        } catch (ex: Exception) {
            logger.warn("Erro ao aplicar heurística Itau: ${ex.message}")
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
