package com.anderson.pdf_converter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "database")
data class DatabaseSecrets(
    var username: String = "",
    var password: String = "",
    var url: String = ""
)

@Component
@ConfigurationProperties(prefix = "api")
data class ApiSecrets(
    var key: String = "",
    var endpoint: String = ""
)