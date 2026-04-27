package com.friendzone.android

import android.app.Application
import org.osmdroid.config.Configuration
import androidx.work.Configuration as WorkConfiguration
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.work.HiltWorkerFactory
import javax.inject.Inject
import com.friendzone.android.data.AppPreferences
import com.friendzone.android.data.ConfigLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FriendZoneApp : Application(), WorkConfiguration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().userAgentValue = packageName

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            val config = ConfigLoader(this@FriendZoneApp).load()
            if (!config.apiBaseUrl.isNullOrBlank()) {
                preferences.setApiBaseUrl(config.apiBaseUrl)
            }
        }
    }

    override val workManagerConfiguration: WorkConfiguration
        get() = WorkConfiguration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
