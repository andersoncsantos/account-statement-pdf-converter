package com.anderson.pdfconverter.domain.ports.`in`

import com.anderson.pdfconverter.domain.model.Product

/**
 * Input port (use-case boundary) for querying a Product by id.
 */
interface GetProductByIdQuery {
    fun getProductById(id: Long): Product?
}