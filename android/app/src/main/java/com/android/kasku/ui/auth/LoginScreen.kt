package com.android.kasku.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Untuk mendapatkan ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes // Pastikan ini diimpor
import com.android.kasku.ui.common.CustomButton
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

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = currentLoginSuccess) {
        if (currentLoginSuccess) {
            authViewModel.resetLoginState()
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            OutlinedTextField(
                value = currentEmail,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = currentErrorMessage != null && currentErrorMessage.contains("email", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector  = image, contentDescription = "Toggle password visibility")
                    }
                },
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

            CustomButton(
                text = "Login",
                onClick = { authViewModel.login() },
                enabled = !currentIsLoading,
                isLoading = currentIsLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate(AppRoutes.REGISTER_SCREEN) {
                    launchSingleTop = true
                }
                authViewModel.resetLoginState()
            }) {
                Text("Belum punya akun? Daftar di sini.", style = MaterialTheme.typography.labelSmall)
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