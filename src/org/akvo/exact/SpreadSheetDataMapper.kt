package org.akvo.exact

import java.text.*
import java.util.*

class SpreadSheetDataMapper {

    private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

    fun invoicesToStrings(result: InvoicesResult): MutableList<List<String>> {
        val invoices = result.d.results
        val values = mutableListOf<List<String>>()
        values.add(listOf("Description", "InvoiceToName", "InvoiceToContactPersonName", "Amount", "Currency", "OrderDate"))
        for (invoice in invoices) {
            val amount = invoice.amountDC.toString()
            val replace = invoice.orderDate.replace("/Date(", "").replace(")/", "")
            val date = Date(replace.toLong())
            val formattedDate = simpleDateFormat.format(date)
            values.add(listOf(invoice.description, invoice.invoiceToName, invoice.invoiceToContactPerson, amount, invoice.currency, formattedDate))
        }
        return values
    }
}
