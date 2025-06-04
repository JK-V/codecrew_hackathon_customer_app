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
        private const val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kYzhjLTBhM2ItMDJjMy01OTNhMGQwMGM0ODEiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkwNjk0ODkiLCJleHAiOjE3NDkxNTU4ODksInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6InZvaXAiLCJyZXNvdXJjZUlkIjoiYTY3ZmYwNzAtMmM4Mi00MGE5LWE2NzctMmQzYzE2ZTcyYmUxIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTc0OTA2OTQ4OX0.VQ3pwK77Ftm9Ko_wsJV0BuscDb5TfMPGVI4Me0YaR1UAamSbYlTcyZ7rJHpq98NLyImbPZoDxTEnvbSxvkklsJK-FsnUAUx2R0tng6ocpMLAp1pC3xH3iHSeDFmN6JMBSMStyc4Ij_jjs1B7Um17KBXuyK-EtgFFiwEJlHOGgbLuuJdLLC8BhO8xGKV0ueaJMc4sDAhpHsY9-ENi3rdMFptml_JCz8b6FBv8hV9TG_mO6nsqcUslGVzl4WbusVLoMbCCxYHTE7V8bOCXTqyv-ZxsUAg_Uuq_RBEd0DSUoY7-yG4EjXfaVX1IV6xcnOcM2pONKxCUjHO9jlIe4QFsHg"

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