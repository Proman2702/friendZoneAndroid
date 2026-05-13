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
import androidx.hilt.navigation.compose.hiltViewModel
import com.friendzone.android.core.location.LocationTrackingManager
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.navigation.AppNavHost
import com.friendzone.android.presentation.permissions.PermissionsGate
import com.friendzone.android.presentation.settings.SettingsViewModel
import com.friendzone.android.presentation.theme.FriendZoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FriendZoneTheme {
                val authViewModel = hiltViewModel<AuthViewModel>()
                val settingsViewModel = hiltViewModel<SettingsViewModel>()
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
                    AppNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}
