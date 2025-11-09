# Technology Stack

## Programming Languages
- **Kotlin 1.9.25**: Primary language with Spring plugin support
- **Java 17**: Target JVM version via toolchain configuration

## Frameworks & Libraries
- **Spring Boot 3.5.7**: Web framework and application container
- **Spring Web**: REST API development with embedded Tomcat
- **Jackson Kotlin Module**: JSON serialization/deserialization for Kotlin
- **Apache PDFBox 2.0.29**: PDF document processing and text extraction

## Build System
- **Gradle 8.14.3**: Build automation with Kotlin DSL
- **Gradle Wrapper**: Ensures consistent build environment across machines

## Testing Framework
- **JUnit 5**: Unit testing platform
- **Spring Boot Test**: Integration testing support
- **Kotlin Test**: Kotlin-specific testing utilities

## Development Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Gradle Tasks
```bash
# View all available tasks
./gradlew tasks

# Build without tests
./gradlew assemble

# Generate JAR file
./gradlew bootJar
```

## Configuration
- **Application Name**: pdf-converter
- **Default Port**: 8080 (Spring Boot default)
- **Package Structure**: com.anderson.pdf_converter
- **Build Output**: build/libs/pdf-converter-0.0.1-SNAPSHOT.jar

## Dependencies Management
- **Maven Central**: Primary repository for dependency resolution
- **Spring Dependency Management Plugin**: Manages Spring Boot BOM versions
- **Kotlin Compiler Options**: JSR-305 strict mode enabled for null safety

## Runtime Requirements
- **Java 17+**: Required for Spring Boot 3.x
- **Memory**: Sufficient heap space for PDF processing (depends on file sizes)
- **File System**: Temporary space for PDF processing operations