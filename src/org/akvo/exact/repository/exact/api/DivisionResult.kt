package org.akvo.exact.repository.exact.api

import com.google.gson.annotations.SerializedName

data class DivisionResult (

    @SerializedName("d") val d : DivisionResultList
)

data class DivisionResultList (

    @SerializedName("results") val results : List<Results>
)

data class Results (

    @SerializedName("__metadata") val __metadata : Metadata,
    @SerializedName("CurrentDivision") val currentDivision : Int
)

data class Metadata (

    @SerializedName("uri") val uri : String,
    @SerializedName("type") val type : String
)
