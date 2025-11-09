package com.anderson.pdf_converter.model

data class Transaction(
    val date: String,
    val description: String,
    val amount: String,
    val balance: String? = null
)

