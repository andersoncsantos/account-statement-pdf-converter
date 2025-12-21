package com.anderson.pdf_converter.application.usecase

import com.anderson.pdf_converter.application.port.`in`.GetProductByIdQuery
import com.anderson.pdf_converter.valkey.data.Product
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ProductService : GetProductByIdQuery {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    // Simulate a database with in-memory map
    private val database = mutableMapOf(
        1L to Product(1L, "Laptop", 999.99),
        2L to Product(2L, "Mouse", 29.99),
        3L to Product(3L, "Keyboard", 79.99)
    )

    @Cacheable(value = ["products"], key = "#id")
    override fun getProductById(id: Long): Product? {
        logger.info("Fetching product from database for id: {}", id)
        // Simulate slow database call
        Thread.sleep(3000)
        return database[id]
    }

    @CacheEvict(value = ["products"], key = "#id")
    fun evictCache(id: Long) {
        logger.info("Evicting cache for product id: {}", id)
    }

    @CacheEvict(value = ["products"], allEntries = true)
    fun evictAllCache() {
        logger.info("Evicting all cache entries")
    }
}