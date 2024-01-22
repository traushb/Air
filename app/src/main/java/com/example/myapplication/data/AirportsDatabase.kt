package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Airport::class, Favorite::class], version = 1, exportSchema = false)
abstract class AirportsDatabase : RoomDatabase() {
    abstract fun airportsDao(): AirportsDao

    companion object {
        @Volatile
        private var Instance: AirportsDatabase? = null

        fun getDatabase(context: Context): AirportsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AirportsDatabase::class.java, "app_database")
                    .createFromAsset("database/airports.db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}