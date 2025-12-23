package com.anderson.pdfconverter.presentation.http

import com.anderson.pdfconverter.domain.ports.`in`.SecretsPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/secrets")
class SecretController(
    private val secretsPort: SecretsPort
) {

    @GetMapping("/database")
    fun getDatabaseSecrets(): Map<String, String> {
        return secretsPort.getDatabaseSecrets()
    }

    @GetMapping("/api")
    fun getApiSecrets(): Map<String, String> {
        return secretsPort.getApiSecrets()
    }

    @GetMapping("/all")
    fun getAllSecrets(): Map<String, Any> {
        return secretsPort.getAllSecrets()
    }
}
