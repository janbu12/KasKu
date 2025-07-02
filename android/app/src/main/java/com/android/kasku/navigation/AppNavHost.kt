package com.android.kasku.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel // Untuk AuthViewModel

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

    NavHost(navController = navController, startDestination = AppRoutes.SPLASH_SCREEN) {
        composable(AppRoutes.SPLASH_SCREEN) {
            SplashScreen(navController = navController)
        }
        composable(AppRoutes.LOGIN_SCREEN) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppRoutes.HOME_SCREEN) {
            MainScreen() // Ini adalah MainScreen placeholder Anda, nanti bisa jadi HomeScreen.kt
        }
        composable(AppRoutes.REGISTER_SCREEN) { // <-- BARU
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        // Tambahkan composable lain untuk rute Anda di sini
    }
}