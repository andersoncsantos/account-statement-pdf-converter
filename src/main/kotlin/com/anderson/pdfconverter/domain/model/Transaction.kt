package com.anderson.pdfconverter.domain.model

data class Transaction(
    val date: String,
    val description: String,
    val amount: String,
    val balance: String? = null
)