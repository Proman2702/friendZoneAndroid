package com.friendzone.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.friendzone.android.ui.AppNavHost
import com.friendzone.android.ui.PermissionsGate
import com.friendzone.android.ui.theme.FriendZoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FriendZoneTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val bootstrapViewModel = hiltViewModel<BootstrapViewModel>()
                    val authViewModel = hiltViewModel<AuthViewModel>()
                    val bootstrapState by bootstrapViewModel.state.collectAsState()
                    val authState by authViewModel.state.collectAsState()
                    if (authState.isLoggedIn) {
                        PermissionsGate(onReady = {
                            bootstrapViewModel.ensureRegistered()
                        })
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(authViewModel = authViewModel)
                        if (bootstrapState.errorMessage != null) {
                            Text(
                                text = bootstrapState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
