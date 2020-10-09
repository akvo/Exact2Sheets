package org.akvo.exact

import java.text.*
import java.util.*

class SpreadSheetDataMapper {

    private val simpleDateFormat = SimpleDateFormat("MM-dd-yyyy")

    fun invoicesToStrings(result: InvoicesResult): MutableList<List<String>> {
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
}
