package com.example.myapplication.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SAVED_QUERIES = stringPreferencesKey("saved_queries")
        const val TAG = "DataStoreRepo"
    }

    suspend fun saveQuery(savedQuery: String) {
        dataStore.edit { query ->
            query[SAVED_QUERIES] = savedQuery
        }
    }

    val savedQuery: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading saved query.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { query ->
            query[SAVED_QUERIES] ?: ""
        }
}