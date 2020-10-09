package org.akvo.exact

import com.google.gson.annotations.SerializedName

data class InvoicesResult (

    @SerializedName("d") val d : D
)

data class D (

    @SerializedName("results") val results : List<Invoice>
)

data class Invoice (

    @SerializedName("AmountDC") val amountDC : Double,
    @SerializedName("Currency") val currency : String,
    @SerializedName("Description") val description : String,
    @SerializedName("InvoiceToContactPersonFullName") val invoiceToContactPerson : String,
    @SerializedName("InvoiceToName") val invoiceToName : String,
    @SerializedName("OrderDate") val orderDate : String
)
