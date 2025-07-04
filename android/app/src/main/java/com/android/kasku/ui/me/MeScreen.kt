package com.android.kasku.ui.me

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.theme.KasKuTheme

@Composable
fun MeScreen(authViewModel: AuthViewModel = viewModel()) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { authViewModel.logout() }) {
            Text("Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeScreenPreview() {
    KasKuTheme {
        MeScreen()
    }
}