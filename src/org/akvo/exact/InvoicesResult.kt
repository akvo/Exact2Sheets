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
    @SerializedName("InvoiceToName") val invoiceToName : String,
    @SerializedName("OrderDate") val orderDate : String,
    @SerializedName("OrderNumber") val orderNumber : Int,
    @SerializedName("Currency") val currency : String
)
