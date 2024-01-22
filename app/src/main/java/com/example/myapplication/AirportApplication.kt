package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.AirportsDatabase
import com.example.myapplication.data.DataStoreRepository

private const val DATASTORE_PREFERENCE_NAME = "datastore_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATASTORE_PREFERENCE_NAME
)

class AirportApplication: Application() {
    val database: AirportsDatabase by lazy { AirportsDatabase.getDatabase(this) }
    val dataStoreRepository: DataStoreRepository by lazy { DataStoreRepository(dataStore) }
}
