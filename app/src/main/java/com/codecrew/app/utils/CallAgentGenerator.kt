package com.codecrew.app.utils

import android.content.Context
import android.util.Log
import com.azure.android.communication.calling.CallAgent
import com.azure.android.communication.calling.CallClient
import com.azure.android.communication.calling.CallClientOptions
import com.azure.android.communication.calling.IncomingCall
import com.azure.android.communication.calling.IncomingCallListener
import com.azure.android.communication.calling.PropertyChangedEvent
import com.azure.android.communication.calling.PropertyChangedListener
import com.azure.android.communication.common.CommunicationTokenCredential

class CallAgentGenerator private constructor(
    private val context: Context
) {

    fun getCallAgent(): CallAgent {
        val callClientOptions = CallClientOptions()
        val callClient = CallClient(callClientOptions)
        val communicationTokenCredential = CommunicationTokenCredential(userToken)
        val callAgent = callClient.createCallAgent(context, communicationTokenCredential).get()

        callAgent.addOnIncomingCallListener(object: IncomingCallListener {
            override fun onIncomingCall(incomingCall: IncomingCall?) {
                Log.d("ACS", "onIncomingCall = ${incomingCall?.callerInfo?.displayName}")
                Log.d("ACS", "onIncomingCall = ${incomingCall?.toString()}")

                //incomingCall?.accept(context)
                incomingCall?.reject()

                incomingCall?.addOnCallEndedListener(object: PropertyChangedListener{
                    override fun onPropertyChanged(args: PropertyChangedEvent?) {
                        Log.d("ACS", "onPropertyChanged = ${args.toString()}")
                    }
                })
            }
        })

        return callAgent
    }

    companion object {
        private const val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kZTRmLTYyZjctZTNjNy01OTNhMGQwMGY4OTAiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkwOTkwNjkiLCJleHAiOjE3NDkxODU0NjksInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQsdm9pcCIsInJlc291cmNlSWQiOiJhNjdmZjA3MC0yYzgyLTQwYTktYTY3Ny0yZDNjMTZlNzJiZTEiLCJyZXNvdXJjZUxvY2F0aW9uIjoidW5pdGVkc3RhdGVzIiwiaWF0IjoxNzQ5MDk5MDY5fQ.H4ZJ5ZfXGAli5FKQGsKm-EaC9C7tWfak2_vAZUGUnLTSL0Co7UgipdQMM3yilMPKxy54V-_bgzp4jrpqzdNPWGOS7BbJ6suFhEHNNePA7sRL0LNunaZFI1pjEXIODxOqSDz9i2HWcZBMGqBjjt8pF9U67ibJ1cdOBis3_PmEjCvmDOHbpFjFqxBZbohH8RKsmR_ZcoKUn66ZEWO5WAEAhi1aQvHg7qgtVirqdaG0wsTvGYV56gIrfZxGkgIZHN3mC5rXgm_aDp3U2524e1iZ4jGnguWMOqIszXse2_Rx_78xfAwtqXMA3ifpUcen6mC0QDR8nYzomuRCsZj-l7ldVg"

        @Volatile
        private var INSTANCE: CallAgentGenerator? = null

        fun getInstance(context: Context): CallAgentGenerator {
            synchronized(CallAgentGenerator::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = CallAgentGenerator(context)
                }
                return INSTANCE as CallAgentGenerator
            }
        }
    }
}