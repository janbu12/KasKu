package com.android.kasku.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.ui.common.CustomButton
import com.android.kasku.ui.theme.KasKuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUsername = authViewModel.registerUsername
    val currentEmail = authViewModel.registerEmail
    val currentPassword = authViewModel.registerPassword
    val currentConfirmPassword = authViewModel.registerConfirmPassword
    val currentIsLoading = authViewModel.isLoading
    val currentErrorMessage = authViewModel.errorMessage
    val currentRegisterSuccess = authViewModel.registerSuccess

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = currentRegisterSuccess) {
        if (currentRegisterSuccess) {
            Toast.makeText(context, "Register berhasil!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            authViewModel.resetRegisterState()
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
                text = "Daftar Akun Baru",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            OutlinedTextField(
                value = currentUsername,
                onValueChange = { authViewModel.onRegisterUsernameChange(it) },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = currentErrorMessage != null && currentErrorMessage.contains("username", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = currentEmail,
                onValueChange = { authViewModel.onRegisterEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = currentErrorMessage != null && currentErrorMessage.contains("email", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { authViewModel.onRegisterPasswordChange(it) },
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                isError = currentErrorMessage != null && currentErrorMessage.contains("password", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = currentConfirmPassword,
                onValueChange = { authViewModel.onRegisterConfirmPasswordChange(it) },
                label = { Text("Konfirmasi Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password Icon") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector  = image, contentDescription = "Toggle confirm password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                isError = currentErrorMessage != null && currentErrorMessage.contains("match", ignoreCase = true) == true
            )

            if (currentErrorMessage != null) {
                Text(
                    text = currentErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            CustomButton(
                text = "Register",
                onClick = { authViewModel.login() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !currentIsLoading,
                isLoading = currentIsLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.popBackStack()
                authViewModel.resetRegisterState()
            }) {
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
