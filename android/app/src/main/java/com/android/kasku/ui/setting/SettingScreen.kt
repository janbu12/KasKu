package com.android.kasku.ui.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.common.CustomButton
import com.android.kasku.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current // Mendapatkan context

    // Hapus Scaffold dari sini
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Padding akan diterapkan oleh NavHost di MainScreen
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isDarkTheme) "Night Theme" else "Light Theme",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { themeViewModel.toggleTheme() }
                    )
                }
            }

            CustomButton(
                onClick = { authViewModel.logout() }, // Pass context to logout
                modifier = Modifier.fillMaxWidth(),
                text = "Logout",
            )
        }
    }
}