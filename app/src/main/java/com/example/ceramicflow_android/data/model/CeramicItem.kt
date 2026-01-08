package com.example.ceramicflow_android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "ceramic_items")
@TypeConverters(CeramicTypeConverter::class, StringListConverter::class)
data class CeramicItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: CeramicType,
    val quantity: Int,
    val description: String = "",
    val userId: String,
    val images: List<String> = emptyList()
)

enum class CeramicType {
    MUG,
    COFFEE_CUP,
    PLATE,
    BOWL,
    VASE,
    OTHER
}


class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }
}