# PDF Converter API

## Project Purpose
A Spring Boot REST API service that converts PDF documents to CSV format, with specialized support for MercadoPago financial statements. The service extracts structured data from PDF documents and transforms it into machine-readable CSV format.

## Key Features
- **PDF to CSV Conversion**: Generic PDF text extraction and CSV conversion capabilities
- **MercadoPago Specialized Parser**: Advanced parsing logic specifically designed for MercadoPago financial statement PDFs
- **RESTful API**: HTTP endpoints for file upload and conversion
- **Multi-page Support**: Handles PDF documents spanning multiple pages with proper data continuity
- **Robust Text Processing**: Advanced regex patterns and text parsing to handle various PDF formats and layouts

## Target Users
- **Financial Teams**: Processing MercadoPago transaction reports for accounting and analysis
- **Data Analysts**: Converting PDF reports into structured data for further processing
- **Automation Systems**: Integrating PDF conversion into larger data processing pipelines
- **Developers**: Building applications that need to extract structured data from PDF documents

## Use Cases
- Converting MercadoPago financial statements to CSV for accounting software import
- Extracting transaction data from PDF reports for financial analysis
- Automating data entry processes that currently rely on manual PDF reading
- Building data pipelines that process financial documents at scale