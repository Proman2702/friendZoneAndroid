package com.friendzone.android.data.remote

import com.friendzone.android.data.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import com.friendzone.android.BuildConfig

class ApiBaseUrlProvider(
    prefs: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //TODO to properties
    private val baseUrlRef = AtomicReference("10.43.190.200")

    init {
        scope.launch {
            prefs.apiBaseUrl.collectLatest { value ->
                baseUrlRef.set(value)
            }
        }
    }

    fun get(): String = baseUrlRef.get()
}


