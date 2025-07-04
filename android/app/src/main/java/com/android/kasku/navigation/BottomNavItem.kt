// app/src/main/java/com/android/kasku/navigation/BottomNavItem.kt
package com.android.kasku.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard_route", "Dashboard", Icons.Default.Dashboard)
    object Structs : BottomNavItem("structs_route", "Struk", Icons.Default.Receipt)
    object Me : BottomNavItem("me_route", "Profil", Icons.Default.AccountCircle)

}