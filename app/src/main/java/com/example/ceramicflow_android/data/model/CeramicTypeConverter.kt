package com.example.ceramicflow_android.data.model

import androidx.room.TypeConverter

class CeramicTypeConverter {
    @TypeConverter
    fun toCeramicType(value: String) = enumValueOf<CeramicType>(value)

    @TypeConverter
    fun fromCeramicType(value: CeramicType) = value.name
}