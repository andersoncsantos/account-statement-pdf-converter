package com.anderson.pdfconverter.infrastructure

import com.anderson.pdfconverter.domain.ports.`in`.SecretsPort
import com.anderson.pdfconverter.infrastructure.configuration.ApiSecrets
import com.anderson.pdfconverter.infrastructure.configuration.DatabaseSecrets
import org.springframework.stereotype.Component

@Component
class SecretsAdapter(
    private val databaseSecrets: DatabaseSecrets,
    private val apiSecrets: ApiSecrets
) : SecretsPort {

    override fun getDatabaseSecrets(): Map<String, String> {
        return mapOf(
            "username" to databaseSecrets.username,
            "url" to databaseSecrets.url,
            "message" to "Password hidden for security"
        )
    }

    override fun getApiSecrets(): Map<String, String> {
        return mapOf(
            "endpoint" to apiSecrets.endpoint,
            "message" to "API key hidden for security"
        )
    }

    override fun getAllSecrets(): Map<String, Any> {
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
