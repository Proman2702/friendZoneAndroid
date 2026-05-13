package com.friendzone.android.data.remote

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class ApiBaseUrlProvider(
    prefs: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val baseUrlRef = AtomicReference(BuildConfig.API_BASE_URL)

    init {
        scope.launch {
            prefs.apiBaseUrl.collectLatest { value ->
                baseUrlRef.set(value)
            }
        }
    }

    fun get(): String = baseUrlRef.get()
}


