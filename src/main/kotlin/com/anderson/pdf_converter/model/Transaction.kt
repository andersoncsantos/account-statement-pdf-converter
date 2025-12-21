package com.anderson.pdf_converter.domain.model

data class Transaction(
    val date: String,
    val description: String,
    val amount: String,
    val balance: String? = null
)
