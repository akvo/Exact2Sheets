package org.akvo.exact.repository.sheets

import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.SalesInvoicesResult
import org.akvo.exact.repository.SpreadSheetDataMapper

class GoogleSheetRepository: SheetRepository {
    private val spreadSheetDataSource = GoogleSheetDataSource()
    private val spreadSheetDataMapper = SpreadSheetDataMapper()

    override suspend fun insertSalesInvoices(salesInvoices: SalesInvoicesResult): String {
        return spreadSheetDataSource.insertToSheet(
            spreadSheetDataMapper.salesInvoicesToStrings(salesInvoices),
            RANGE_SHEET1
        )
    }

    override suspend fun insertReceivablesInvoices(receivableInvoices: ReceivableInvoicesResult): String {
        return spreadSheetDataSource.insertToSheet(
            spreadSheetDataMapper.receivableInvoicesToStrings(receivableInvoices),
            RANGE_SHEET2
        )
    }
}
