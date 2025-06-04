package com.codecrew.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codecrew.app.login.LoginScreen
/*import com.codecrew.app.utils.UserPreferences
import com.codecrew.app.navigation.Screen
import com.codecrew.app.sing_up.SignUpScreen*/

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var startDestination by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(key1 = Unit) { // Run once on composition
                /*val hasSignedUp = UserPreferences.hasCompletedSignUp(applicationContext)
                startDestination = if (hasSignedUp) {
                    Screen.Login.route
                } else {
                    Screen.SignUp.route
                }*/
            }

            if (startDestination != null) {
                YourApp(startDestination = startDestination!!)
            }
        }
    }
}

@Composable
fun YourApp(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        /*composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.ManageDevices.route) {
            ManageDevicesScreen(navController = navController)
        }*/
    }
}