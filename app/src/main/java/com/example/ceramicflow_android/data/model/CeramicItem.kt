package com.example.ceramicflow_android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "ceramic_items")
@TypeConverters(CeramicTypeConverter::class)
data class CeramicItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: CeramicType,
    val quantity: Int,
    val description: String = "",
    val userId: String
)

enum class CeramicType {
    MUG,
    COFFEE_CUP,
    PLATE,
    BOWL,
    VASE,
    OTHER
}
