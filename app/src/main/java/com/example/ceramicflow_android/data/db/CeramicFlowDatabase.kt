package com.example.ceramicflow_android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.model.User

@Database(entities = [CeramicItem::class, User::class, Booking::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CeramicFlowDatabase : RoomDatabase() {

    abstract fun ceramicItemDao(): CeramicItemDao
    abstract fun userDao(): UserDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: CeramicFlowDatabase? = null

        fun getDatabase(context: Context): CeramicFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CeramicFlowDatabase::class.java,
                    "ceramic_flow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}