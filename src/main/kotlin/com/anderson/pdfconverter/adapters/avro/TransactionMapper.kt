package com.anderson.pdfconverter.adapters.avro

import com.anderson.pdf_converter.avro.TransactionAvro
import com.anderson.pdfconverter.domain.model.Transaction

/**
 * Mapper object for converting between domain Transaction model and Avro TransactionAvro model.
 */
object TransactionMapper {

    /**
     * Converts a domain Transaction to an Avro TransactionAvro.
     *
     * @param transaction The domain Transaction to convert
     * @return The corresponding TransactionAvro
     */
    fun toAvro(transaction: Transaction): TransactionAvro {
        return TransactionAvro(
            transaction.date,
            transaction.description,
            transaction.amount,
            transaction.balance
        )
    }

    /**
     * Converts an Avro TransactionAvro back to a domain Transaction.
     *
     * @param avro The Avro TransactionAvro to convert
     * @return The corresponding domain Transaction
     */
    fun fromAvro(avro: TransactionAvro): Transaction {
        return Transaction(
            date = avro.date,
            description = avro.description,
            amount = avro.amount,
            balance = avro.balance
        )
    }
}