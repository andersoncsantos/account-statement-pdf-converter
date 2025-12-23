package com.anderson.pdf_converter

import com.anderson.pdfconverter.domain.model.Transaction
import com.anderson.pdfconverter.infrastructure.avro.TransactionMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for TransactionMapper using Kotest.
 * 
 * **Feature: schema-registry-integration, Property 1: Serialization round-trip preserves data**
 * **Validates: Requirements 7.5**
 */
class TransactionMapperPropertyTest : StringSpec({
    
    "Property 1: Serialization round-trip preserves data" {
        // Generator for Transaction objects
        val transactionGen = Arb.bind(
            Arb.string(1..50),      // date
            Arb.string(1..200),     // description
            Arb.string(1..20),      // amount
            Arb.string(1..20).orNull() // balance (optional)
        ) { date, description, amount, balance ->
            Transaction(date, description, amount, balance)
        }
        
        // Property: For any Transaction, serializing to Avro and deserializing back
        // should produce an equivalent Transaction
        checkAll(100, transactionGen) { original ->
            val avro = TransactionMapper.toAvro(original)
            val result = TransactionMapper.fromAvro(avro)
            
            result shouldBe original
        }
    }
})
