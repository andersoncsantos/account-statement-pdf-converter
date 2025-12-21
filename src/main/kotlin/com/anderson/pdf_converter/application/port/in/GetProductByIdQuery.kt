package com.anderson.pdf_converter.application.port.`in`

import com.anderson.pdf_converter.valkey.data.Product

/**
 * Input port (use-case boundary) for querying a Product by id.
 */
interface GetProductByIdQuery {
    fun getProductById(id: Long): Product?
}

