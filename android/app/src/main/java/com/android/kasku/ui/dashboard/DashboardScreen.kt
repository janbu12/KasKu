package com.android.kasku.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.android.kasku.ui.theme.KasKuTheme
import androidx.compose.material3.MaterialTheme // Import MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    // Hapus Scaffold di sini
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Gunakan MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Ini Halaman Dashboard!", fontSize = 24.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    KasKuTheme {
        DashboardScreen()
    }
}