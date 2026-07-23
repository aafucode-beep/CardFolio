package com.cardfolio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsDataStore {
    private val KEY_SERVER_URL = stringPreferencesKey("server_url")
    const val DEFAULT_URL = "http://10.0.2.2:8000/"

    fun getServerUrl(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_SERVER_URL] ?: DEFAULT_URL
        }
    }

    suspend fun setServerUrl(context: Context, url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = url
        }
    }
}
