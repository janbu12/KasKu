package com.android.kasku.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.ui.theme.KasKuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // State dari ViewModel untuk Register
    val username by remember { derivedStateOf { authViewModel.registerUsername } }
    val email by remember { derivedStateOf { authViewModel.registerEmail } }
    val password by remember { derivedStateOf { authViewModel.registerPassword } }
    val isLoading by remember { derivedStateOf { authViewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { authViewModel.errorMessage } }
    val registerSuccess by remember { derivedStateOf { authViewModel.registerSuccess } }

    // Effect untuk menangani navigasi setelah register berhasil
    LaunchedEffect(key1 = registerSuccess) {
        if (registerSuccess) {
            // Setelah register sukses, navigasi ke layar Login atau Home
            navController.navigate(AppRoutes.LOGIN_SCREEN) {
                popUpTo(AppRoutes.REGISTER_SCREEN) { inclusive = true } // Hapus RegisterScreen dari backstack
            }
            authViewModel.resetRegisterState() // Reset state ViewModel setelah navigasi
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
                text = "Daftar Akun KasKu",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { authViewModel.onRegisterUsernameChange(it) },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = errorMessage != null && errorMessage?.contains("username", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { authViewModel.onRegisterEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = errorMessage != null && errorMessage?.contains("email", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { authViewModel.onRegisterPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                isError = errorMessage != null && errorMessage?.contains("password", ignoreCase = true) == true ||
                        errorMessage != null && errorMessage?.contains("symbol", ignoreCase = true) == true ||
                        errorMessage != null && errorMessage?.contains("uppercase", ignoreCase = true) == true ||
                        errorMessage != null && errorMessage?.contains("lowercase", ignoreCase = true) == true ||
                        errorMessage != null && errorMessage?.contains("number", ignoreCase = true) == true
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { authViewModel.registerUser() }, // Panggil fungsi register ke backend
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Daftar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(AppRoutes.LOGIN_SCREEN) {
                popUpTo(AppRoutes.REGISTER_SCREEN) { inclusive = true }
            } }) {
                Text("Sudah punya akun? Login di sini.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    KasKuTheme {
        RegisterScreen(navController = rememberNavController())
    }
}