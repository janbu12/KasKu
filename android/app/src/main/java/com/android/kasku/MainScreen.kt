package com.android.kasku

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.navigation.BottomNavItem
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.common.AddStructFab
import com.android.kasku.ui.common.BottomNavigationBar
import com.android.kasku.ui.dashboard.DashboardScreen
import com.android.kasku.ui.me.ProfileScreen
import com.android.kasku.ui.profile.ProfileUiState // Pastikan import ini ada
import com.android.kasku.ui.profile.ProfileViewModel
import com.android.kasku.ui.setting.SettingScreen
import com.android.kasku.ui.structs.StructScreen
import com.android.kasku.ui.theme.ThemeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val bottomNavController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()
    val context = LocalContext.current // Dapatkan context di sini

    // Panggil fetchUserProfile saat MainScreen pertama kali dimuat
    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile(context)
    }

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dapatkan username dari ProfileViewModel
    val profileUiState by profileViewModel.uiState.collectAsState()
    val username = if (profileUiState is ProfileUiState.Success) {
        (profileUiState as ProfileUiState.Success).userData.username
    } else {
        "User" // Default jika belum dimuat atau ada error
    }

    // Hanya tampilkan TopAppBar kustom untuk Dashboard
    val showCustomTopBar = currentRoute == BottomNavItem.Dashboard.route

    Scaffold(
        topBar = {
            if (showCustomTopBar) {
                // Top Bar kustom untuk Dashboard
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Foto Profil
                            Box (
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(45.dp)
                                    .border(border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.user_icon), // Gunakan user_icon yang sudah ada
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary // Gunakan onBackground untuk teks di TopAppBar
                                )
                                Text(
                                    text = username, // Gunakan username yang didapat
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary // Gunakan onBackground
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f)) // Dorong ikon notifikasi ke kanan
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background // Latar belakang TopAppBar
                    )
                )
            } else {
                // Top Bar default untuk layar lain
                val topBarTitle = when (currentRoute) {
                    BottomNavItem.Structs.route -> "Struk"
                    BottomNavItem.Profile.route -> "Profil"
                    BottomNavItem.Setting.route -> "Setting"
                    AppRoutes.ADD_STRUCT_SCREEN -> "Tambah Struk"
                    else -> "KasKu" // Default jika tidak ada yang cocok
                }
                TopAppBar(
                    title = { Text(text = topBarTitle, color = MaterialTheme.colorScheme.primary) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
            )
        },
        floatingActionButton = {
            AddStructFab {
                navController.navigate(AppRoutes.ADD_STRUCT_SCREEN)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
            composable(BottomNavItem.Structs.route) {
                StructScreen(
                    navToEdit = { structId ->
                        navController.navigate("${AppRoutes.EDIT_STRUCT_SCREEN}/$structId")
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(profileViewModel)
            }
            composable(BottomNavItem.Setting.route) {
                SettingScreen(authViewModel, themeViewModel)
            }
        }
    }
}