package com.anderson.pdf_converter.valkey.data

import java.io.Serializable

data class Product(
    val id: Long? = null,
    val name: String? = null,
    val price: Double? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}