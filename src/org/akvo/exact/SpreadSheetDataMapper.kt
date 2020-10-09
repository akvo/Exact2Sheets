package org.akvo.exact

import java.text.*
import java.util.*

class SpreadSheetDataMapper {

    private val simpleDateFormat = SimpleDateFormat("MM-dd-yyyy")

    fun salesInvoicesToStrings(result: SalesInvoicesResult): MutableList<List<String>> {
        val invoices = result.d.results
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
            val date: Date? = invoice.orderDate?.replace("/Date(", "")?.replace(")/", "")?.toLong()?.let { Date(it) }
            val formattedDate: String = if (date != null) simpleDateFormat.format(date) else ""
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

    fun outStandingInvoicesToStrings(result: OutstandingInvoicesResult): MutableList<List<String>> {
        val invoices = result.d.results
        val values = mutableListOf<List<String>>()
        values.add(
            listOf(
                "OutstandingReceivableInvoiceCount",
                "OutstandingReceivableInvoiceAmount",
                "OverdueReceivableInvoiceCount",
                "OverdueReceivableInvoiceAmount",
                "CurrencyCode"
            )
        )
        for (invoice in invoices) {
            values.add(
                listOf(
                    invoice.outstandingReceivableInvoiceCount?.toString() ?: "",
                    invoice.outstandingReceivableInvoiceAmount?.toString()  ?: "",
                    invoice.overdueReceivableInvoiceCount?.toString()  ?: "",
                    invoice.overdueReceivableInvoiceAmount?.toString()  ?: "",
                    invoice.currencyCode ?: ""
                )
            )
        }
        return values
    }
}
