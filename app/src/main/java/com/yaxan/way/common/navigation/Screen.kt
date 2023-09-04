package com.yaxan.way.common.navigation

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
}
