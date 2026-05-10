package com.friendzone.android.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.navigation.AppNavHost
import com.friendzone.android.presentation.permissions.PermissionsGate
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
                Surface(color = MaterialTheme.colorScheme.background) {
                    val authViewModel = hiltViewModel<AuthViewModel>()
                    Box(modifier = Modifier.fillMaxSize()) {
                        PermissionsGate(onReady = { })
                        AppNavHost(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}


