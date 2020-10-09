package org.akvo.exact

import com.google.gson.annotations.SerializedName

data class OutstandingInvoicesResult (

    @SerializedName("d") val d : OutstandingD
)

data class OutstandingD (

    @SerializedName("results") val results : List<OutstandingInvoice>
)

data class OutstandingInvoice (

    @SerializedName("CurrencyCode") val currencyCode : String?,
    @SerializedName("OutstandingReceivableInvoiceCount") val outstandingReceivableInvoiceCount : Int?,
    @SerializedName("OutstandingReceivableInvoiceAmount") val outstandingReceivableInvoiceAmount : Double?,
    @SerializedName("OverdueReceivableInvoiceCount") val overdueReceivableInvoiceCount : Int?,
    @SerializedName("OverdueReceivableInvoiceAmount") val overdueReceivableInvoiceAmount : Double?
)
