# Development Guidelines

## Code Quality Standards

### Package Structure (5/5 files)
- Use snake_case for package names: `com.anderson.pdf_converter`
- Maintain consistent package hierarchy with domain-based organization
- Keep all application classes under the same root package for component scanning

### Class Naming Conventions (5/5 files)
- Use PascalCase for class names: `MercadoPagoPdfToCsvService`, `PdfController`
- Service classes end with "Service": `PdfToCsvService`, `MercadoPagoPdfToCsvService`
- Controller classes end with "Controller": `PdfController`
- Application class matches project name: `PdfConverterApplication`

### Import Organization (5/5 files)
- Group imports by: Java standard library, third-party libraries, Spring framework
- Use explicit imports, avoid wildcard imports
- Order: `java.*`, `org.apache.*`, `org.springframework.*`

## Structural Conventions

### Spring Annotations (4/5 files)
- Use `@Service` for business logic classes
- Use `@RestController` for REST API endpoints
- Use `@SpringBootApplication` for main application class
- Use `@RequestMapping` for base controller paths

### Constructor Injection (2/5 files)
```kotlin
class PdfController(
    private val pdfToCsvService: MercadoPagoPdfToCsvService
)
```
- Prefer constructor injection over field injection
- Use `private val` for injected dependencies
- Single constructor parameter per line for readability

### Method Signatures (4/5 files)
```kotlin
fun convertPdfToCsv(inputStream: InputStream): String
```
- Use descriptive method names with clear intent
- Accept `InputStream` for file processing to support various sources
- Return `String` for CSV content rather than writing to files

## Practices Followed

### Resource Management (3/5 files)
```kotlin
val document = PDDocument.load(inputStream)
val text = stripper.getText(document)
document.close()
```
- Always close PDDocument resources after use
- Load documents from InputStream for flexibility
- Use PDFTextStripper for text extraction

### String Processing (3/5 files)
```kotlin
val lines = content.lines()
    .map { it.trim() }
    .filter { it.isNotBlank() }
```
- Chain string operations using functional style
- Use `trim()` to remove whitespace
- Filter out blank lines consistently

### Regex Patterns (2/5 files)
```kotlin
val datePattern = Regex("""(\d{2}[-/]\d{2}[-/]\d{4})""")
val headerRegex = Regex("""Data\s+Descrição\s+ID da operação\s+Valor\s+Saldo""")
```
- Use raw strings `"""` for regex patterns to avoid escaping
- Create descriptive variable names for regex patterns
- Use capturing groups for data extraction

### CSV Generation (3/5 files)
```kotlin
val csv = StringBuilder("Data,Descrição,ID da operação,Valor (R$)\n")
csv.append("${csvSafe(data)},${csvSafe(descricao)},${csvSafe(id)},${csvSafe(valor)}\n")
```
- Use StringBuilder for efficient string concatenation
- Include headers as first line
- Implement CSV escaping with `csvSafe()` helper method

## Semantic Patterns

### Error Handling (2/5 files)
```kotlin
if (detailStart == -1) {
    println("ERRO: Não encontrou 'DETALHE DOS MOVIMENTOS'")
    return csv.toString()
}
```
- Use early returns for error conditions
- Print debug messages for troubleshooting
- Return partial results rather than throwing exceptions

### Debug Logging (1/5 files)
```kotlin
println("=== TEXTO COMPLETO (${text.length} chars) ===")
println(text.substring(0, minOf(2000, text.length)))
```
- Use println for debug output during development
- Include data length information in debug messages
- Truncate large outputs using `minOf()` for readability

### Collection Processing (3/5 files)
```kotlin
val pendingDescriptionParts = mutableListOf<String>()
pendingDescriptionParts.addAll(descParts)
```
- Use mutable collections for accumulating data during processing
- Prefer `addAll()` for bulk operations
- Use descriptive names for temporary collections

### HTTP Response Patterns (1/5 files)
```kotlin
return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv")
    .contentType(MediaType.TEXT_PLAIN)
    .body(csv)
```
- Use ResponseEntity builder pattern for HTTP responses
- Set appropriate headers for file downloads
- Use MediaType constants instead of string literals

### Testing Structure (1/5 files)
```kotlin
@SpringBootTest
class PdfConverterApplicationTests {
    @Test
    fun contextLoads() {
    }
}
```
- Use `@SpringBootTest` for integration tests
- Include basic context loading test as smoke test
- Follow naming convention: `[ClassName]Tests`