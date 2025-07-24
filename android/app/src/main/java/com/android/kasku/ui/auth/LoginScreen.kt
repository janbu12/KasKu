package com.android.kasku.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.android.kasku.R

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
    val isUserLoggedIn by rememberUpdatedState(authViewModel.isUserLoggedIn)
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = currentLoginSuccess, key2 = isUserLoggedIn) {
        if (currentLoginSuccess) {
            authViewModel.resetLoginState()
        }

        if (isUserLoggedIn) {
            navController.navigate(AppRoutes.APP_GRAPH_ROOT) {
                popUpTo(AppRoutes.LOGIN_SCREEN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 47.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_picture),
                    contentDescription = "Login Illustration",
                    modifier = Modifier.defaultMinSize(

                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(43.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 26.dp)
                        .align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = currentEmail,
                    onValueChange = { authViewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
                        errorLeadingIconColor = Color.Red,
                        errorTrailingIconColor = Color.Red
                    ),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
                        errorLeadingIconColor = Color.Red,
                        errorTrailingIconColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).align(Alignment.CenterHorizontally),
                    isError = currentErrorMessage != null && currentErrorMessage?.contains("password", ignoreCase = true) == true ||
                            currentErrorMessage != null && currentErrorMessage?.contains("credentials", ignoreCase = true) == true
                )

                if (currentErrorMessage != null) {
                    Text(
                        text = currentErrorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 26.dp).align(Alignment.CenterHorizontally)
                    )
                }

                CustomButton(
                    text = "Login",
                    onClick = { authViewModel.login(
                        context = context
                    ) },
                    enabled = !currentIsLoading,
                    isLoading = currentIsLoading,
                    modifier = Modifier.fillMaxWidth().height(46.dp).align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        navController.navigate(AppRoutes.REGISTER_SCREEN) {
                            launchSingleTop = true
                        }
                        authViewModel.resetLoginState()
                    }) {
                    Text("Belum punya akun? Daftar di sini.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
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