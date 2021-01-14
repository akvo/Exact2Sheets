package org.akvo.exact.repository.sheets

import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.SpreadSheetDataMapper
import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.SalesInvoice

class GoogleSheetRepository: SheetRepository {
    private val spreadSheetDataSource = GoogleSheetDataSource()
    private val spreadSheetDataMapper = SpreadSheetDataMapper()

    override suspend fun insertSalesInvoices(invoices: List<SalesInvoice>): String {
        return spreadSheetDataSource.insertToSheet(
            spreadSheetDataMapper.salesInvoicesToStrings(invoices),
            RANGE_SHEET1
        )
    }

    override suspend fun insertReceivablesInvoices(receivableInvoices: List<ReceivableInvoice>): String {
        return spreadSheetDataSource.insertToSheet(
            spreadSheetDataMapper.receivableInvoicesToStrings(receivableInvoices),
            RANGE_SHEET2
        )
    }
}
