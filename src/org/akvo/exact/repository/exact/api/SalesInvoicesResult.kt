package org.akvo.exact.repository.exact.api

import com.google.gson.annotations.SerializedName

data class SalesInvoicesResult (

    @SerializedName("d") val d : SalesD
)

data class SalesD (

    @SerializedName("results") val results : List<SalesInvoice>
)

data class SalesInvoice (

    @SerializedName("AmountDC") val amountDC : Double?,
    @SerializedName("Currency") val currency : String?,
    @SerializedName("Description") val description : String?,
    @SerializedName("InvoiceToContactPersonFullName") val invoiceToContactPerson : String?,
    @SerializedName("InvoiceToName") val invoiceToName : String?,
    @SerializedName("OrderDate") val orderDate : String?
)
