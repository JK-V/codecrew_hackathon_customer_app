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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codecrew.app.audio.AcsManager
import com.codecrew.app.audio.AudioCallScreen
import com.codecrew.app.audio.AudioCallViewModel
import com.codecrew.app.audio.AudioCallViewModelFactory
import com.codecrew.app.audio.incomingcall.IncomingCallScreen
import com.codecrew.app.audio.incomingcall.IncomingCallViewModel
import com.codecrew.app.audio.incomingcall.IncomingCallViewModelFactory
import com.codecrew.app.login.LoginScreen
import com.codecrew.app.utils.UserPreferences
import com.codecrew.app.navigation.Screen
import com.codecrew.app.sing_up.SignUpScreen
import com.codecrew.app.utils.CallAgentGenerator
import com.codecrew.app.utils.CallAgentGenerator.Companion.userToken
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity() : ComponentActivity() {
    private lateinit var acsManager: AcsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acsManager = AcsManager.getInstance(this)
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
                YourApp(startDestination = startDestination!!, acsManager)
            }
        }
        getAllPermissions()

        // Obtain AcsManager instance (singleton or from DI)


        // Observe AcsManager for navigation triggers globally or in a launching Activity

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

            //CallAgentGenerator.getInstance(this).getCallAgent().registerPushNotification(token)

            AcsManager.getInstance(this).createCallAgent(acsToken = userToken, fcmToken = token)
        }
    }
}

@Composable
fun YourApp(startDestination: String, acsManager: AcsManager) {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {

        // This is a simplified example. In a real app, this observation
        // might be in your MainActivity or a component that lives as long as the app.
        acsManager.callUiState.collect { state ->
            if (state.incomingCall != null && navController.currentDestination?.route !=Screen.IncomingScreen.route) {
                // Only navigate if we're not already there to avoid loops
                // and there's an actual incoming call being signaled by AcsManager
                navController.navigate(Screen.IncomingScreen.route) {
                    // Avoid multiple copies of the incoming call screen
                    launchSingleTop = true
                }
            } else if (state.activeCall != null && state.incomingCall == null &&
                navController.currentDestination?.route !=Screen.AudioScreen.route) {
                // If a call becomes active (e.g., after accepting or making one)
                // and we're not on the audio call screen, navigate there.
                // This handles the transition after accepting from IncomingCallScreen.
                navController.navigate(Screen.AudioScreen.route) {
                    // Potentially pop IncomingCallScreen off the stack
                    popUpTo("incomingCallScreenRoute") { inclusive = true }
                    launchSingleTop = true
                }
            } else if (state.incomingCall == null && state.activeCall == null &&
                (navController.currentDestination?.route == Screen.IncomingScreen.route ||
                        navController.currentDestination?.route == "audioCallScreenRoute") ) {
                // If no incoming or active call, and we are on one of the call screens,
                // consider popping back. This logic needs care to avoid unwanted navigation.
                // For instance, if the call ended, AudioCallScreen's ViewModel might handle its own dismissal.
                // If an incoming call was rejected/missed, IncomingCallScreen handles it.
            }
        }
    }
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
        composable(Screen.AudioScreen.route) {
            val context = LocalContext.current.applicationContext


            val audioCallViewModel: AudioCallViewModel =
                viewModel(
                factory = AudioCallViewModelFactory(context, acsManager)
            )
            AudioCallScreen(
                viewModel = audioCallViewModel,
                onCallEnded = {
                    // navController.popBackStack()
                    // Or navigate to a post-call summary, etc.
                    // This logic needs to be robustly tied to the call actually ending.
                }
            )
        }
        composable(Screen.IncomingScreen.route) {
            val context = LocalContext.current.applicationContext
            val incomingCallViewModel: IncomingCallViewModel = viewModel(
                factory = IncomingCallViewModelFactory(acsManager)
            )
            IncomingCallScreen(
                viewModel = incomingCallViewModel,
                onCallAccepted = {
                    // The LaunchedEffect observing acsManager.callUiState for an activeCall
                    // should handle navigating to the audioCallScreenRoute.
                    // So, this lambda might not even need to explicitly navigate here if that global observer is robust.
                    // For now, let's assume direct navigation intent:
                    // navController.navigate("audioCallScreenRoute") {
                    //    popUpTo("incomingCallScreenRoute") { inclusive = true }
                    //    launchSingleTop = true
                    // }
                    // If the global LaunchedEffect for navigation is active, this specific navigation
                    // might become redundant or could even conflict. It's often better to have a single
                    // source of truth for navigation based on AcsManager's state.
                },
                onCallRejectedOrMissed = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        // If no back stack (e.g., app launched directly into call),
                        // navigate to a default screen or close.
                        // navController.navigate("yourMainAppScreenRoute") {
                        //     popUpTo("incomingCallScreenRoute") { inclusive = true }
                        // }
                        // This part depends on your app's desired behavior.
                    }
                }
            )
        }
    }
}