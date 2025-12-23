package com.anderson.pdfconverter.adapters.http

import com.anderson.pdfconverter.configuration.ApiSecrets
import com.anderson.pdfconverter.configuration.DatabaseSecrets
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/secrets")
class SecretController(
    private val databaseSecrets: DatabaseSecrets,
    private val apiSecrets: ApiSecrets
) {

    @GetMapping("/database")
    fun getDatabaseSecrets(): Map<String, String> {
        println("=== Database Secrets ===")
        println("Username: ${databaseSecrets.username}")
        println("Password: ${databaseSecrets.password}")
        println("URL: ${databaseSecrets.url}")
        println("========================")

        return mapOf(
            "username" to databaseSecrets.username,
            "url" to databaseSecrets.url,
            "message" to "Password hidden for security"
        )
    }

    @GetMapping("/api")
    fun getApiSecrets(): Map<String, String> {
        println("=== API Secrets ===")
        println("Key: ${apiSecrets.key}")
        println("Endpoint: ${apiSecrets.endpoint}")
        println("===================")

        return mapOf(
            "endpoint" to apiSecrets.endpoint,
            "message" to "API key hidden for security"
        )
    }

    @GetMapping("/all")
    fun getAllSecrets(): Map<String, Any> {
        println("=== All Secrets Retrieved ===")
        println("Database - Username: ${databaseSecrets.username}")
        println("Database - URL: ${databaseSecrets.url}")
        println("API - Endpoint: ${apiSecrets.endpoint}")
        println("=============================")

        return mapOf(
            "database" to mapOf(
                "username" to databaseSecrets.username,
                "url" to databaseSecrets.url
            ),
            "api" to mapOf(
                "endpoint" to apiSecrets.endpoint
            )
        )
    }
}