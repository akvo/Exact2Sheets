package org.akvo.exact.repository.sheets

import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.SalesInvoice

interface SheetRepository {

    suspend fun insertSalesInvoices(invoices: List<SalesInvoice>): String
    suspend fun insertReceivablesInvoices(receivableInvoices: List<ReceivableInvoice>): String
}
