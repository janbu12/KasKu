package com.android.kasku

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.android.kasku.ui.me.ProfileScreen
import com.android.kasku.ui.profile.ProfileViewModel
import com.android.kasku.ui.setting.SettingScreen
import com.android.kasku.ui.structs.StructsScreen
import com.android.kasku.ui.theme.ThemeViewModel

@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val bottomNavController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()

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
                ProfileScreen(profileViewModel)
            }
            composable(BottomNavItem.Setting.route) {
                SettingScreen(authViewModel, themeViewModel)
            }
        }
    }
}
