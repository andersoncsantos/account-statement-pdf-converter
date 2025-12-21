package com.anderson.pdf_converter.adapters.inbound.web

import com.anderson.pdf_converter.application.port.`in`.GetProductByIdQuery
import com.anderson.pdf_converter.valkey.data.Product
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(private val getProductByIdQuery: GetProductByIdQuery) {

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<Product> {
        val startTime = System.currentTimeMillis()
        val product = getProductByIdQuery.getProductById(id)
        val endTime = System.currentTimeMillis()

        println("Request took: ${endTime - startTime}ms")

        return product?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/cache/{id}")
    fun evictCache(@PathVariable id: Long): ResponseEntity<String> {
        // The cache eviction methods are on ProductService; if needed, create a separate port for cache operations.
        return ResponseEntity.badRequest().body("Not implemented: evict by id via port")
    }

    @DeleteMapping("/cache")
    fun evictAllCache(): ResponseEntity<String> {
        return ResponseEntity.badRequest().body("Not implemented: evict all via port")
    }
}