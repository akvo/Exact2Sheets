package org.akvo.exact.repository

import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.SalesInvoice
import java.text.*
import java.util.*

class SpreadSheetDataMapper {

    private val simpleDateFormat = SimpleDateFormat("MM-dd-yyyy")

    fun salesInvoicesToStrings(invoices: List<SalesInvoice>): MutableList<List<String>> {
        val values = mutableListOf<List<String>>()
        values.add(
            listOf(
                "Project",
                "Payers Name",
                "Contact Person",
                "Amount",
                "Currency",
                "To be sent date"
            )
        )
        for (invoice in invoices) {
            val amount: String = invoice.amountDC?.toString() ?: ""
            val formattedDate: String = formattedDate(invoice.orderDate)
            values.add(
                listOf(
                    invoice.description ?: "",
                    invoice.invoiceToName ?: "",
                    invoice.invoiceToContactPerson ?: "",
                    amount,
                    invoice.currency ?: "",
                    formattedDate
                )
            )
        }
        return values
    }

    private fun formattedDate(dateString: String?): String {
        val date: Date? = dateString?.replace("/Date(", "")?.replace(")/", "")?.toLong()?.let { Date(it) }
        return if (date != null) simpleDateFormat.format(date) else ""
    }

    fun receivableInvoicesToStrings(invoices: List<ReceivableInvoice>): MutableList<List<String>> {
        val values = mutableListOf<List<String>>()
        values.add(
            listOf(
                "Project name",
                "Payers name",
                "Amount",
                "Currency",
                "Invoice number",
                "Invoice date",
                "Due date"
            )
        )
        for (invoice in invoices) {
            val formattedDueDate: String = formattedDate(invoice.dueDate)
            val formattedInvoiceDate: String = formattedDate(invoice.invoiceDate)
            values.add(
                listOf(
                    invoice.description ?: "",
                    invoice.accountName ?: "",
                    invoice.amount?.toString() ?: "",
                    invoice.currencyCode ?: "",
                    invoice.invoiceNumber?.toString() ?: "",
                    formattedInvoiceDate,
                    formattedDueDate
                )
            )
        }
        return values
    }
}
