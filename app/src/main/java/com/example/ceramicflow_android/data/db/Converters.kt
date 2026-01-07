package com.example.ceramicflow_android.data.db

import androidx.room.TypeConverter
import com.example.ceramicflow_android.data.model.CeramicItem
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromCeramicItem(ceramicItem: CeramicItem?): String? {
        return Gson().toJson(ceramicItem)
    }

    @TypeConverter
    fun toCeramicItem(ceramicItemString: String?): CeramicItem? {
        return Gson().fromJson(ceramicItemString, CeramicItem::class.java)
    }
}