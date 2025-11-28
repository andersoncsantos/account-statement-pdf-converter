package com.anderson.pdf_converter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class PdfConverterApplication

fun main(args: Array<String>) {
	runApplication<PdfConverterApplication>(*args)
}
