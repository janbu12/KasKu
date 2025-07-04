package com.android.kasku.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel // Untuk AuthViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.navigation

import com.android.kasku.ui.splash.SplashScreen // Pastikan import ini benar
import com.android.kasku.ui.auth.LoginScreen
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.MainScreen
import com.android.kasku.ui.auth.RegisterScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Inisialisasi ViewModel di sini, atau gunakan Hilt jika sudah di setup
    val authViewModel: AuthViewModel = viewModel()

    val isUserLoggedIn by rememberUpdatedState(authViewModel.isUserLoggedIn)
    val authCheckCompleted by rememberUpdatedState(authViewModel.authCheckCompleted)

    var splashAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = splashAnimationFinished, key2 = authCheckCompleted, key3 = isUserLoggedIn) {
        if (splashAnimationFinished && authCheckCompleted) {
            val splashScreenRouteId = navController.graph.findStartDestination().id

            if (isUserLoggedIn) {
                navController.popBackStack(splashScreenRouteId, inclusive = true)
                navController.popBackStack(AppRoutes.LOGIN_SCREEN, inclusive = true)

                navController.navigate(AppRoutes.APP_GRAPH_ROOT) {
                    launchSingleTop = true
                }
            } else {
                navController.popBackStack(AppRoutes.APP_GRAPH_ROOT, inclusive = true)
                navController.popBackStack(splashScreenRouteId, inclusive = true)

                navController.navigate(AppRoutes.LOGIN_SCREEN) {
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.SPLASH_SCREEN) {
        composable(AppRoutes.SPLASH_SCREEN) {
            SplashScreen(navController = navController, onAnimationFinished = { splashAnimationFinished = true })
        }
        composable(AppRoutes.LOGIN_SCREEN) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppRoutes.REGISTER_SCREEN) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        navigation(
            startDestination = BottomNavItem.Dashboard.route,
            route = AppRoutes.APP_GRAPH_ROOT
        ) {
            composable(BottomNavItem.Dashboard.route) {
                MainScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(BottomNavItem.Structs.route) {
                MainScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(BottomNavItem.Me.route) {
                MainScreen(navController = navController, authViewModel = authViewModel)
            }
        }
    }
}