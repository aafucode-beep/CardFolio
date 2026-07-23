package com.cardfolio

import android.app.Application
import com.cardfolio.data.ApiClient
import com.cardfolio.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CardFolioApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val savedUrl = SettingsDataStore.getServerUrl(this@CardFolioApp).first()
            ApiClient.updateBaseUrl(savedUrl)
        }
    }
}
