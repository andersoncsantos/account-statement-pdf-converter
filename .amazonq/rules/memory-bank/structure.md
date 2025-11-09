# Project Structure

## Directory Organization

```
pdf-converter/
├── src/main/kotlin/com/anderson/pdf_converter/
│   ├── PdfConverterApplication.kt          # Spring Boot main application
│   ├── PdfController.kt                    # REST API endpoints
│   ├── PdfToCsvService.kt                  # Generic PDF conversion service
│   └── MercadoPagoPdfToCsvService.kt       # Specialized MercadoPago parser
├── src/main/resources/
│   └── application.yaml                    # Application configuration
├── src/test/kotlin/com/anderson/pdf_converter/
│   └── PdfConverterApplicationTests.kt     # Unit tests
├── gradle/wrapper/                         # Gradle wrapper files
├── build.gradle.kts                        # Build configuration
└── settings.gradle.kts                     # Project settings
```

## Core Components

### Application Layer
- **PdfConverterApplication.kt**: Spring Boot entry point with main method and application configuration
- **PdfController.kt**: REST controller handling HTTP requests for PDF conversion operations

### Service Layer
- **PdfToCsvService.kt**: Generic service for basic PDF to CSV conversion using PDFBox
- **MercadoPagoPdfToCsvService.kt**: Specialized service with advanced parsing logic for MercadoPago financial statements

### Configuration
- **application.yaml**: Spring Boot configuration including application name and server settings

## Architectural Patterns

### Layered Architecture
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic for PDF processing and conversion
- **No Data Layer**: Stateless service processing files without persistence

### Dependency Injection
- Uses Spring's `@Service` and `@RestController` annotations for component scanning
- Constructor-based dependency injection for service components

### Single Responsibility Principle
- Separate services for generic PDF conversion vs. specialized MercadoPago parsing
- Clear separation between HTTP handling and business logic

## Technology Stack Integration
- **Spring Boot**: Provides web framework, dependency injection, and auto-configuration
- **Apache PDFBox**: Core PDF processing library for text extraction
- **Kotlin**: Primary programming language with Java interoperability
- **Gradle**: Build system with Kotlin DSL configuration