package com.android.kasku.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.kasku.navigation.BottomNavItem
import com.android.kasku.ui.theme.Green40


@Composable
fun BottomNavigationBar(
    navController: NavController,
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Structs,
        BottomNavItem.Profile,
        BottomNavItem.Setting
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentPadding = PaddingValues(horizontal = 12.dp),
            actions = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                AddItem(items[0], currentRoute, navController)

                // Item 2: Structs
                AddItem(items[1], currentRoute, navController)

                // Spacer untuk membuat ruang di tengah agar FAB bisa melayang di sana
                // Ini menekan item-item di kanan ke kanan
                Spacer(modifier = Modifier.weight(1f))

                // Item 3: Profile
                AddItem(items[2], currentRoute, navController)

                // Item 4: Setting
                AddItem(items[3], currentRoute, navController)
            },
        )
    }
}

    @Composable
    fun AddStructFab(onFabClick: () -> Unit) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            FloatingActionButton(
                onClick = onFabClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Struct"
                )
            }

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                modifier = Modifier,
                text = "Add Struct",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis

                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.label,
                    tint = Color.Unspecified
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
                selectedIconColor = MaterialTheme.colorScheme.tertiary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                unselectedTextColor = Color.Gray,
            )
        )
    }