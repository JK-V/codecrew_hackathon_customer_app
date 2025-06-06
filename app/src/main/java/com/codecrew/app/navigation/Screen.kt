package com.codecrew.app.navigation

sealed class Screen(val route: String) {
    object SignUp : Screen("signup_screen")
    object Login : Screen("login_screen")
    object Main : Screen("main_screen")
    object ManageDevices : Screen("manage_devices_screen")
    object AudioScreen : Screen("audio_call_screen")
    object IncomingScreen : Screen("incoming_call_screen")
}