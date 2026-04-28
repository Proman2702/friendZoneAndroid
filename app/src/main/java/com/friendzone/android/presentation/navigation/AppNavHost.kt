package com.friendzone.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.friendzone.android.presentation.auth.AuthViewModel
import com.friendzone.android.presentation.events.EventsScreen
import com.friendzone.android.presentation.auth.ForgotPasswordScreen
import com.friendzone.android.presentation.auth.LoginScreen
import com.friendzone.android.presentation.auth.RegisterScreen
import com.friendzone.android.presentation.zones.ZoneEditScreen
import com.friendzone.android.presentation.zones.ZoneListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ZONES = "zones"
    const val EVENTS = "events"
    const val ZONE_EDIT = "zone_edit"
}

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigateClearingStack(Routes.ZONES, Routes.LOGIN)
        } else if (navController.currentDestination?.route != Routes.LOGIN) {
            navController.navigateClearingStack(Routes.LOGIN, Routes.ZONES)
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
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
                        navController.popBackStack()
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
        composable(Routes.ZONES) {
            ZoneListScreen(
                onCreateZone = { navController.navigate(Routes.ZONE_EDIT) },
                onOpenEvents = { navController.navigate(Routes.EVENTS) },
                onEditZone = { navController.navigate("${Routes.ZONE_EDIT}?id=$it") }
            )
        }
        composable(Routes.EVENTS) {
            EventsScreen(onBack = { navController.popBackStack() })
        }
        composable("${Routes.ZONE_EDIT}?id={id}") { entry ->
            ZoneEditScreen(
                zoneId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() }
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


