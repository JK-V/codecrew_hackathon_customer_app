package com.codecrew.app

import android.util.Log
import com.azure.android.communication.calling.PushNotificationInfo
import com.codecrew.app.utils.CallAgentGenerator
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "ACS MyFirebaseMsgService"

    /**
     * Called when a message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        Log.d(TAG, "Message data payload: " + remoteMessage.data)
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            try {
                /*val notification: IncomingCallInformation =
                    IncomingCallInformation.fromMap(pushNotificationMessageDataFromFCM)
                val handlePushNotificationFuture: Future =
                    callAgent.handlePushNotification(notification).get()*/


                CallAgentGenerator.getInstance(this).getCallAgent().handlePushNotification(
                    PushNotificationInfo.fromMap(remoteMessage.data))
            } catch (e: Exception) {
                println("Something went wrong while handling the Incoming Calls Push Notifications.")
            }

        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        CallAgentGenerator.getInstance(this).getCallAgent().registerPushNotification(token)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
}