package com.anderson.pdf_converter.exception

class ParserNotFoundException(message: String) : RuntimeException(message)
class PdfParsingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
