package com.android.kasku.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.kasku.navigation.BottomNavItem
import com.android.kasku.ui.theme.Green40

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Structs,
        BottomNavItem.Me
    )

    NavigationBar (
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = Color.LightGray
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            AddItem(
                item = item,
                currentRoute = currentRoute,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    item: BottomNavItem,
    currentRoute: String?,
    navController: NavController
) {
    NavigationBarItem(
        label = {
            Text(text = item.label)
        },
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label
            )
        },
        selected = currentRoute == item.route,
        onClick = {
            // Jika item yang dipilih sudah merupakan rute saat ini, tidak perlu navigasi
            if (currentRoute != item.route) {
                navController.navigate(item.route) {
                    // Pop up ke start destination dari BottomNav Graph
                    // untuk menghindari banyak instance dari layar yang sama
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Hindari membuat banyak salinan dari tujuan yang sama
                    launchSingleTop = true
                    // Restore state saat kembali ke tujuan yang sudah ada di back stack
                    restoreState = true
                }
            }
        },
        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = Color.White,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
        )
    )
}