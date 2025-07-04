package com.android.kasku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.kasku.navigation.BottomNavItem
import com.android.kasku.ui.auth.AuthViewModel
import com.android.kasku.ui.common.BottomNavigationBar
import com.android.kasku.ui.dashboard.DashboardScreen
import com.android.kasku.ui.me.MeScreen
import com.android.kasku.ui.structs.StructsScreen
import com.android.kasku.ui.theme.KasKuTheme

@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val bottomNavController = rememberNavController()

    Scaffold (
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController )
        }
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
            composable(BottomNavItem.Me.route) {
                MeScreen(authViewModel)
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    KasKuTheme {
        MainScreen(navController = rememberNavController())
    }
}