package org.akvo.exact.repository.exact.api

import com.google.gson.annotations.SerializedName

data class ReceivableInvoicesResult (

    @SerializedName("d") val d : ReceivableD,
)

data class ReceivableD (

    @SerializedName("results") val results : List<ReceivableInvoice>,
    @SerializedName("__next") val nextUrl: String?
)

data class ReceivableInvoice (

    @SerializedName("AccountName") val accountName : String?,
    @SerializedName("Amount") val amount : Double?,
    @SerializedName("CurrencyCode") val currencyCode : String?,
    @SerializedName("Description") val description : String?,
    @SerializedName("DueDate") val dueDate : String?,
    @SerializedName("InvoiceDate") val invoiceDate : String?,
    @SerializedName("InvoiceNumber") val invoiceNumber : Int?
)
