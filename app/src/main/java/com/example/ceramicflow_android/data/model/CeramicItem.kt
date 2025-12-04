package com.example.ceramicflow_android.data.model

data class CeramicItem(
    val id: String,
    val name: String,
    val type: CeramicType,
    val quantity: Int,
    val description: String = ""
)

enum class CeramicType {
    MUG,
    COFFEE_CUP,
    PLATE,
    BOWL,
    VASE,
    OTHER
}
