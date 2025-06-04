package com.codecrew.app

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azure.android.communication.calling.CallAgent
import com.azure.android.communication.calling.CallAgentOptions
import com.azure.android.communication.calling.CallClient
import com.azure.android.communication.calling.CallClientOptions
import com.azure.android.communication.calling.TelecomManagerOptions
import com.azure.android.communication.common.CommunicationTokenCredential
import com.codecrew.app.login.LoginScreen
import com.codecrew.app.model.CustomerData
import com.codecrew.app.model.RetrofitClient
import com.codecrew.app.utils.UserPreferences
import com.codecrew.app.navigation.Screen
import com.codecrew.app.sing_up.SignUpScreen
import com.codecrew.app.utils.CallAgentGenerator
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var startDestination by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(key1 = Unit) { // Run once on composition
                val hasSignedUp = UserPreferences.hasCompletedSignUp(applicationContext)
                startDestination = if (hasSignedUp) {
                    Screen.Login.route
                } else {
                    Screen.SignUp.route
                }
            }

            if (startDestination != null) {
                YourApp(startDestination = startDestination!!)
            }
        }
        getAllPermissions()
    }

    private fun getAllPermissions() {
        val requiredPermissions = mutableListOf<String>().apply {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.CAMERA)
            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            add(android.Manifest.permission.READ_PHONE_STATE)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requiredPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val permissionsToAskFor = mutableListOf<String>()

        for (requiredPermission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(requiredPermission)
            }
        }

        if (permissionsToAskFor.isNotEmpty()) {
            ActivityCompat.requestPermissions(this@MainActivity, permissionsToAskFor.toTypedArray(), 1);
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        Log.d("ACS", "inside onRequestPermissionsResult")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("ACS", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("ACS", "Current FCM Token: $token")

            CallAgentGenerator.getInstance(this).getCallAgent().registerPushNotification(token)
        }
    }
}

@Composable
fun YourApp(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.SignUp.route) {
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
        }
    }
}