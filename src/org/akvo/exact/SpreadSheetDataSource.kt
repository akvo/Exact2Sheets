package org.akvo.exact

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.text.SimpleDateFormat
import java.util.*

const val GOOGLE_SHEET_ID = "1RpsFsmLCOfNmeRLDOiEzvQqBF1uhMAeJQB8ZicQExlE"

private const val APPLICATION_NAME = "exact2sheets"
private const val RANGE = "Sheet1!A1:Z1000"

class SpreadSheetDataSource {
    private val transport = GoogleNetHttpTransport.newTrustedTransport()
    private val jacksonFactory = JacksonFactory.getDefaultInstance()

    private val sheetsService: Sheets =
        Sheets.Builder(transport, jacksonFactory, AppCredentials.local)
            .setApplicationName(APPLICATION_NAME)
            .build()

    private val driveService =
        Drive.Builder(transport, jacksonFactory, AppCredentials.local)
            .setApplicationName(APPLICATION_NAME)
            .build()

    private val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

    fun insertToSheet(result: InvoicesResult): String {
        val values = getDataToInsert(result)
        val spreadsheetId = getSheetId(false)

        val valueRange = ValueRange()
        valueRange.majorDimension = "ROWS"
        valueRange.range = RANGE
        @Suppress("UNCHECKED_CAST")
        valueRange.setValues(values as List<MutableList<Any>>?)
        val data: List<ValueRange> = mutableListOf(valueRange)

        val requestBody = BatchUpdateValuesRequest()
        requestBody.data = data
        requestBody.valueInputOption = "USER_ENTERED"
        try {
            sheetsService.spreadsheets().values().clear(spreadsheetId, RANGE, ClearValuesRequest()).execute()
            sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, requestBody).execute()
            updateSpreadSheetPermissions(spreadsheetId)
            println("Data successfully inserted to spreadsheet ID: $spreadsheetId")
            return spreadsheetId
        } catch (e: Exception) {
            System.err.print(e)
            return ""
        }
    }

    /**
     * Anyone with akvo.org account can Edit the sheet
     */
    private fun updateSpreadSheetPermissions(spreadsheetId: String) {
        val permission = Permission()
        permission.domain = "akvo.org"
        permission.type = "domain"
        permission.role = "writer"
        driveService.permissions().create(spreadsheetId, permission).execute()
    }

    private fun getDataToInsert(result: InvoicesResult): MutableList<List<String>> {
        val invoices = result.d.results
        val values = mutableListOf<List<String>>()
        values.add(listOf("OrderNumber", "InvoiceToName", "Amount", "Currency", "OrderDate"))
        for (invoice in invoices) {
            val amount = invoice.amountDC.toString()
            val replace = invoice.orderDate.replace("/Date(", "").replace(")/", "")
            val date = Date(replace.toLong())
            val formattedDate = simpleDateFormat.format(date)
            values.add(listOf(invoice.orderNumber.toString(), invoice.invoiceToName, amount, invoice.currency, formattedDate))
        }
        return values
    }

    private fun getSheetId(createNew: Boolean): String {
        return if (createNew) {
            val spreadsheet = Spreadsheet()
                .setProperties(SpreadsheetProperties().setTitle("Invoices from Exact"))
            val createdSpreadSheet: Spreadsheet =
                sheetsService.Spreadsheets().create(spreadsheet).setFields("spreadsheetId")
                    .execute()
            createdSpreadSheet.spreadsheetId
        } else {
            GOOGLE_SHEET_ID
        }
    }
}

object AppCredentials {
    val local: HttpRequestInitializer by lazy {
        HttpCredentialsAdapter(GoogleCredentials.fromStream(pathStream).createScoped(scopes))
    }

    private val pathStream
        get() = AppCredentials::class.java.getResourceAsStream(credentialsFilePath)
            ?: error("Resource not found: $credentialsFilePath")

    private val scopes = listOf(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE)
}

private const val credentialsFilePath = "/credentials.json"