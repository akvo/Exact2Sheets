package org.akvo.exact.repository.sheets

import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.SalesInvoicesResult

interface SheetRepository {

    suspend fun insertSalesInvoices(salesInvoices: SalesInvoicesResult): String
    suspend fun insertReceivablesInvoices(receivableInvoices: ReceivableInvoicesResult): String
}