package com.friendzone.android.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.repository.AuthRepository
import com.friendzone.android.core.location.LocationTrackingManager
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.navigation.AppNavHost
import com.friendzone.android.presentation.permissions.PermissionsGate
import com.friendzone.android.presentation.settings.SettingsViewModel
import com.friendzone.android.presentation.theme.FriendZoneTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var prefs: AppPreferences
    @Inject lateinit var authRepository: AuthRepository

    private lateinit var authViewModel: AuthViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel = ViewModelProvider(
            viewModelStore,
            AuthViewModel.Factory(prefs, authRepository)
        )[AuthViewModel::class.java]

        settingsViewModel = ViewModelProvider(
            viewModelStore,
            SettingsViewModel.Factory(prefs, applicationContext)
        )[SettingsViewModel::class.java]

        setContent {
            FriendZoneTheme {
                val settingsState by settingsViewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    settingsViewModel.load()
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    PermissionsGate(
                        onReady = {
                            val intervalMinutes =
                                settingsState.locationUpdateIntervalMinutes.toDoubleOrNull()?.coerceAtLeast(0.1) ?: 1.0
                            LocationTrackingManager.start(this@MainActivity, intervalMinutes)
                        }
                    )
                    AppNavHost(
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
