package com.friendzone.android.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.friendzone.android.presentation.account.AccountScreen
import com.friendzone.android.presentation.account.InvitationsScreen
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.auth.ForgotPasswordScreen
import com.friendzone.android.presentation.auth.LoginScreen
import com.friendzone.android.presentation.auth.RegisterScreen
import com.friendzone.android.presentation.friends.FriendsScreen
import com.friendzone.android.presentation.map.MapScreen
import com.friendzone.android.presentation.map.MapViewModel
import com.friendzone.android.presentation.settings.SettingsViewModel
import com.friendzone.android.presentation.settings.SettingsScreen
import com.friendzone.android.presentation.zones.ZoneEditScreen
import com.friendzone.android.presentation.zones.ZoneListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAP = "map"
    const val ZONES = "zones"
    const val FRIENDS = "friends"
    const val ACCOUNT = "account"
    const val SETTINGS = "settings"
    const val ZONE_EDIT = "zone_edit"
    const val INVITATIONS = "invitations"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.MAP, "Карта", Icons.Default.LocationOn),
    BottomDestination(Routes.ZONES, "Зоны", Icons.AutoMirrored.Filled.List),
    BottomDestination(Routes.FRIENDS, "Друзья", Icons.Default.SupervisorAccount),
    BottomDestination(Routes.ACCOUNT, "Аккаунт", Icons.Default.Person)
)

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.state.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigateClearingStack(Routes.MAP, Routes.LOGIN)
        } else if (navController.currentDestination?.route != Routes.LOGIN) {
            navController.navigateClearingStack(Routes.LOGIN, Routes.MAP)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigateToMainRoute(route) },
                    onCreateZone = {
                        navController.navigateToMainRoute(Routes.MAP)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    errorMessage = authState.errorMessage,
                    infoMessage = authState.infoMessage,
                    apiBaseUrl = authState.apiBaseUrl,
                    onLogin = authViewModel::login,
                    onOpenRegistration = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.REGISTER)
                    },
                    onOpenForgotPassword = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    },
                    onSaveApiBaseUrl = authViewModel::saveApiBaseUrl,
                    onMessagesShown = authViewModel::clearMessages
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    errorMessage = authState.errorMessage,
                    onRegister = { name, email, password ->
                        authViewModel.register(name, email, password) {
                            navController.navigateClearingStack(Routes.MAP, Routes.LOGIN)
                        }
                    },
                    onBack = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onMessagesShown = authViewModel::clearMessages
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    errorMessage = authState.errorMessage,
                    infoMessage = authState.infoMessage,
                    onRecover = authViewModel::recoverPassword,
                    onBack = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onMessagesShown = authViewModel::clearMessages
                )
            }
            composable(Routes.MAP) {
                val mapViewModel = hiltViewModel<MapViewModel>()
                MapScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    viewModel = mapViewModel
                )
            }
            composable(Routes.ZONES) {
                ZoneListScreen()
            }
            composable(Routes.FRIENDS) {
                FriendsScreen()
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    onOpenInvitations = { navController.navigate(Routes.INVITATIONS) },
                    onLogout = authViewModel::logout
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = settingsViewModel
                )
            }
            composable(Routes.INVITATIONS) {
                InvitationsScreen(onBack = { navController.popBackStack() })
            }
            composable("${Routes.ZONE_EDIT}?id={id}") { entry ->
                ZoneEditScreen(
                    zoneId = entry.arguments?.getString("id"),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCreateZone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF151034))
            .navigationBarsPadding()
            .height(78.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                destination = bottomDestinations[0],
                selected = currentRoute == bottomDestinations[0].route,
                onClick = { onNavigate(bottomDestinations[0].route) }
            )
            BottomItem(
                destination = bottomDestinations[1],
                selected = currentRoute == bottomDestinations[1].route,
                onClick = { onNavigate(bottomDestinations[1].route) }
            )
            CenterAddButton(onClick = onCreateZone)
            BottomItem(
                destination = bottomDestinations[2],
                selected = currentRoute == bottomDestinations[2].route,
                onClick = { onNavigate(bottomDestinations[2].route) }
            )
            BottomItem(
                destination = bottomDestinations[3],
                selected = currentRoute == bottomDestinations[3].route,
                onClick = { onNavigate(bottomDestinations[3].route) }
            )
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) Color(0xFF4BB7F0) else Color.White
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = destination.label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun RowScope.CenterAddButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFFE3874F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Создать зону",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun NavHostController.navigateClearingStack(route: String, popUpToRoute: String) {
    navigate(route) {
        popUpTo(popUpToRoute) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToMainRoute(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}
