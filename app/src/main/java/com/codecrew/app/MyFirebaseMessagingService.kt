package com.codecrew.app

import android.util.Log
import com.azure.android.communication.calling.PushNotificationInfo
import com.codecrew.app.audio.AcsManager
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
        //val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kZTU5LWJhNWUtYTg0Mi0wNGJkNDU2MGViNDIiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkwOTk3NDYiLCJleHAiOjE3NDkxODYxNDYsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQsdm9pcCIsInJlc291cmNlSWQiOiJhNjdmZjA3MC0yYzgyLTQwYTktYTY3Ny0yZDNjMTZlNzJiZTEiLCJyZXNvdXJjZUxvY2F0aW9uIjoidW5pdGVkc3RhdGVzIiwiaWF0IjoxNzQ5MDk5NzQ2fQ.tFuUmdZ2d5KQiZ8nz27yiQNBuJqJWXbE29FLXq_uSg7p1pQ74K40avbH3sTcuOMIgfJx5id9WGhRJhKS5q8oXeBieYj1ZsRV2CluEFAYpLvrgi-fOJVqJ1SAAtBco8vy8c6MWcBhLb8CH0UkxqVHzdo2P3ID35lDaFX6kRqugpBZ83TSXmbE_5YtwK___5H51QOjUhJp_s7-FOgsEArFzHJrvjfHO92CAuN6tXA_1JgydkPhyLHUrI4cSSJyXc3LMR_8I32qLr9D2wMSCeHjIhrwaswNxa0q4HmCrdanNm5PtcQ-BicCXP87cAO188TlxCefnIvC4wTdDuT5hMFmVA"

        Log.d(TAG, "Message data payload: " + remoteMessage.data)
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            try {
                /*val notification: IncomingCallInformation =
                    IncomingCallInformation.fromMap(pushNotificationMessageDataFromFCM)
                val handlePushNotificationFuture: Future =
                    callAgent.handlePushNotification(notification).get()*/


                /*CallAgentGenerator.getInstance(this).getCallAgent().handlePushNotification(
                    PushNotificationInfo.fromMap(remoteMessage.data))*/
                AcsManager.getInstance(this).callAgent.handlePushNotification(
                    PushNotificationInfo.fromMap(remoteMessage.data))

            } catch (e: Exception) {
                Log.d(TAG,"Something went wrong while handling the Incoming Calls Push Notifications. ${e.message}")
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
        //CallAgentGenerator.getInstance(this).getCallAgent().registerPushNotification(token)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
}