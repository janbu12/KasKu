package com.android.kasku.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Untuk mendapatkan ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes // Pastikan ini diimpor
import com.android.kasku.ui.theme.KasKuTheme // Sesuaikan tema Anda

@OptIn(ExperimentalMaterial3Api::class) // Untuk Scaffold dan OutlinedTextField
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentEmail = authViewModel.email
    val currentPassword = authViewModel.password
    val currentIsLoading = authViewModel.isLoading
    val currentErrorMessage = authViewModel.errorMessage
    val currentLoginSuccess = authViewModel.loginSuccess


    // Effect untuk menangani navigasi setelah login berhasil
    LaunchedEffect(key1 = currentLoginSuccess) {
        if (currentLoginSuccess) {
            navController.navigate(AppRoutes.HOME_SCREEN) { // Navigasi ke Home Screen setelah login
                popUpTo(AppRoutes.LOGIN_SCREEN) { inclusive = true } // Hapus LoginScreen dari backstack
            }
            authViewModel.resetState() // Reset state ViewModel setelah navigasi
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login ke KasKu",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = currentEmail,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = currentErrorMessage != null && currentErrorMessage?.contains("email", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                isError = currentErrorMessage != null && currentErrorMessage?.contains("password", ignoreCase = true) == true ||
                        currentErrorMessage != null && currentErrorMessage?.contains("credentials", ignoreCase = true) == true
            )

            if (currentErrorMessage != null) {
                Text(
                    text = currentErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { authViewModel.login() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !currentIsLoading // Disable button saat loading
            ) {
                if (currentIsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(AppRoutes.REGISTER_SCREEN) }) {
                Text("Belum punya akun? Daftar di sini.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    KasKuTheme {
        LoginScreen(navController = rememberNavController())
    }
}