package org.akvo.exact

import java.text.*
import java.util.*

class SpreadSheetDataMapper {

    private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

    fun invoicesToStrings(result: InvoicesResult): MutableList<List<String>> {
        val invoices = result.d.results
        val values = mutableListOf<List<String>>()
        values.add(
            listOf(
                "Description",
                "Payers Name",
                "InvoiceToContactPersonName",
                "Amount",
                "Currency",
                "OrderDate"
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
}
