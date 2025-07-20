// app/src/main/java/com/android/kasku/navigation/BottomNavItem.kt
package com.android.kasku.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.kasku.R

sealed class BottomNavItem(val route: String, val label: String, @DrawableRes val iconRes: Int) {
    object Dashboard : BottomNavItem("dashboard_route", "Dashboard", R.drawable.chart_icon)
    object Structs : BottomNavItem("structs_route", "Struk", R.drawable.notepad_icon)
    object Profile : BottomNavItem("profile_route", "Profil", R.drawable.user_icon)
    object Setting : BottomNavItem("setting_route", "Setting", R.drawable.settings_icon)

}