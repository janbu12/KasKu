package com.android.kasku.ui.structs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Tambahkan import Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Tambahkan import padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // Tambahkan import dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AddStructScreen() {
    // --- MULAI PERUBAHAN DI SINI: Hapus Scaffold dan Surface yang bersarang ---
    // Konten layar ini akan langsung menjadi Box berikut
    Column( // Menggunakan Column agar bisa tambahkan padding dari NavHost
        modifier = Modifier.fillMaxSize()
            // .padding(paddingValues) // paddingValues akan datang dari NavHost di MainScreen
            .padding(16.dp), // Padding internal jika diperlukan
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Alignment.CenterVertically as Arrangement.Vertical
    ) {
        Text(text = "Ini Halaman Add Struct!", fontSize = 24.sp)
    }
    // --- AKHIR PERUBAHAN DI SINI ---
}