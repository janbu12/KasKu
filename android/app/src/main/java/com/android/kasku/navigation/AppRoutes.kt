package com.android.kasku.navigation

object AppRoutes {
    const val SPLASH_SCREEN = "splash_screen"
    const val LOGIN_SCREEN = "login_screen"
    const val REGISTER_SCREEN = "register_screen" // Akan dibuat nanti
    const val HOME_SCREEN = "home_screen" // Layar tujuan setelah login/splash
    // Tambahkan rute lain di sini
    const val APP_GRAPH_ROOT = "app_graph_root"
    const val WELCOME_SCREEN = "welcome_screen"
    const val ADD_STRUCT_SCREEN = "add_struct"
    const val EDIT_STRUCT_SCREEN = "edit_struct"

    fun appGraphRouteWithTab(tabRoute: String) = "app_graph_root?startTab=$tabRoute"
}