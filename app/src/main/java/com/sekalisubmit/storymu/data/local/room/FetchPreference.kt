package com.sekalisubmit.storymu.data.local.room

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FetchPreference private constructor(private val dataStore: DataStore<Preferences>){

    private val fetchKEY = booleanPreferencesKey("fetch")

    fun getFetch(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[fetchKEY] ?: true
        }
    }

    suspend fun saveFetch(fetch: Boolean) {
        dataStore.edit { preferences ->
            preferences[fetchKEY] = fetch
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FetchPreference? = null

        fun getInstance(dataStore: DataStore<Preferences>): FetchPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = FetchPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}