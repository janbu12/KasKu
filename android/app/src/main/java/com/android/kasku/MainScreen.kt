package com.android.kasku

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.navigation.BottomNavItem
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.common.AddStructFab
import com.android.kasku.ui.common.BottomNavigationBar
import com.android.kasku.ui.dashboard.DashboardScreen
import com.android.kasku.ui.me.MeScreen
import com.android.kasku.ui.setting.SettingScreen
import com.android.kasku.ui.structs.StructsScreen
import com.android.kasku.ui.theme.Green40
import com.android.kasku.ui.theme.KasKuTheme

@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val bottomNavController = rememberNavController()

    Scaffold(
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
                StructsScreen()
            }
            composable(BottomNavItem.Profile.route) {
                MeScreen()
            }
            composable(BottomNavItem.Setting.route) {
                SettingScreen(authViewModel)
            }
        }
    }
}
