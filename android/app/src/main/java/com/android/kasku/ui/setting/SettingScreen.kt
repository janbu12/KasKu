package com.android.kasku.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dark Theme", fontSize = 18.sp)
        Switch(
            checked = isDarkTheme,
            onCheckedChange = { themeViewModel.toggleTheme() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { authViewModel.logout() }) {
            Text("Logout")
        }
    }
}
