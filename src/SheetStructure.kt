package org.akvo.exact

data class SheetStructure (

    val range : String,
    val majorDimension : String,
    val values : MutableList<List<String>>
)
