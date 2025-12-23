package com.anderson.pdfconverter.domain.ports.`in`

interface SecretsPort {
    fun getDatabaseSecrets(): Map<String, String>
    fun getApiSecrets(): Map<String, String>
    fun getAllSecrets(): Map<String, Any>
}
