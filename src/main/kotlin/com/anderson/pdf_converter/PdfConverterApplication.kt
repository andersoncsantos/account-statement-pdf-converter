package com.anderson.pdf_converter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableKafka
@EnableCaching
class PdfConverterApplication

fun main(args: Array<String>) {
	runApplication<PdfConverterApplication>(*args)
}
