package com.friendzone.android.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.friendzone.android.presentation.account.AccountScreen
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.auth.ForgotPasswordScreen
import com.friendzone.android.presentation.auth.LoginScreen
import com.friendzone.android.presentation.auth.RegisterScreen
import com.friendzone.android.presentation.friends.FriendsScreen
import com.friendzone.android.presentation.map.MapScreen
import com.friendzone.android.presentation.map.MapViewModel
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
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.state.collectAsState()
    val mapViewModel = hiltViewModel<MapViewModel>()
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
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navController.navigateToMainRoute(route) },
                    onCreateZone = {
                        // Диалог зоны открываем на карте, потому что точка создания берётся из центра карты.
                        mapViewModel.requestCreateDialog()
                        navController.navigateToMainRoute(Routes.MAP)
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(if (showBottomBar) padding else PaddingValues(0.dp))
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    errorMessage = authState.errorMessage,
                    infoMessage = authState.infoMessage,
                    onLogin = authViewModel::login,
                    onOpenRegistration = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.REGISTER)
                    },
                    onOpenForgotPassword = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
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
                    }
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
                    }
                )
            }
            composable(Routes.MAP) {
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
                AccountScreen(onLogout = authViewModel::logout)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
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
    // Кастомная форма нужна, чтобы повторить вырез под центральную кнопку из референса.
    val notchShape = rememberNotchedShape()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp),
            color = Color(0xFF151034),
            shape = notchShape,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
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
                Spacer(modifier = Modifier.width(84.dp))
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

        FloatingActionButton(
            onClick = onCreateZone,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(68.dp),
            containerColor = Color(0xFFE3874F),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Создать зону",
                modifier = Modifier.size(34.dp)
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
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = destination.label,
            color = tint,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun rememberNotchedShape(): Shape {
    val density = LocalDensity.current
    return remember(density) {
        object : Shape {
            override fun createOutline(
                size: androidx.compose.ui.geometry.Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                return with(density) {
                    // Строим вырез вручную, потому что стандартная NavigationBar такой notch не поддерживает.
                    val notchRadius = 38.dp.toPx()
                    val notchDepth = 28.dp.toPx()
                    val cornerRadius = 26.dp.toPx()
                    val centerX = size.width / 2f
                    val notchStart = centerX - notchRadius - 18.dp.toPx()
                    val notchEnd = centerX + notchRadius + 18.dp.toPx()

                    val path = Path().apply {
                        moveTo(cornerRadius, 0f)
                        lineTo(notchStart, 0f)
                        cubicTo(
                            notchStart + 8.dp.toPx(),
                            0f,
                            centerX - notchRadius,
                            notchDepth,
                            centerX,
                            notchDepth
                        )
                        cubicTo(
                            centerX + notchRadius,
                            notchDepth,
                            notchEnd - 8.dp.toPx(),
                            0f,
                            notchEnd,
                            0f
                        )
                        lineTo(size.width - cornerRadius, 0f)
                        arcTo(
                            rect = Rect(
                                left = size.width - cornerRadius * 2,
                                top = 0f,
                                right = size.width,
                                bottom = cornerRadius * 2
                            ),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(size.width, size.height - cornerRadius)
                        arcTo(
                            rect = Rect(
                                left = size.width - cornerRadius * 2,
                                top = size.height - cornerRadius * 2,
                                right = size.width,
                                bottom = size.height
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(cornerRadius, size.height)
                        arcTo(
                            rect = Rect(
                                left = 0f,
                                top = size.height - cornerRadius * 2,
                                right = cornerRadius * 2,
                                bottom = size.height
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(0f, cornerRadius)
                        arcTo(
                            rect = Rect(
                                left = 0f,
                                top = 0f,
                                right = cornerRadius * 2,
                                bottom = cornerRadius * 2
                            ),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        close()
                    }
                    Outline.Generic(path)
                }
            }
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
