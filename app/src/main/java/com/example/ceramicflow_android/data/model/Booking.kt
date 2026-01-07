package com.example.ceramicflow_android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Reprezintă datele necesare pentru a crea un obiect ceramic nou.
 */
data class NewCeramicData(
    val name: String,
    val type: String,
    val quantity: Int,
    val description: String
)

/**
 * Reprezintă o cerere de creare a unei programări, incluzând datele pentru un obiect nou.
 */
data class BookingRequest(
    val date: String,
    val time: String,
    val userId: String,
    val newCeramic: NewCeramicData
)

/**
 * Reprezintă o programare completă, stocată atât local, cât și pe server.
 */
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey
    val id: String,
    val date: String,
    val time: String,
    val userId: String,
    val ceramic: CeramicItem,
    var isSynced: Boolean = false // Flag pentru a urmări starea de sincronizare
)
