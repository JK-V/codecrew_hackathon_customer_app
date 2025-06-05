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
        private const val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kZTQwLTMzOWQtYTg0Mi0wNGJkNDU2MGU5NzMiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkwOTgwNzQiLCJleHAiOjE3NDkxODQ0NzQsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6InZvaXAiLCJyZXNvdXJjZUlkIjoiYTY3ZmYwNzAtMmM4Mi00MGE5LWE2NzctMmQzYzE2ZTcyYmUxIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTc0OTA5ODA3NH0.FLnt9J7AZ5N1Cgb9_0Fa1v_5n1NpLckmP5nxcuMJagUQuZAlaKFtGDBsIbG0BhU_bTQiT9dDMnbUDR7zhbns5f1giEL80Ny7pvXlLCM2n-BFrPoTny9a8-IhzNor4b9yYlFb4eE37hLl4Jyi_rLLw8QkYLaSCBPdThA7B2JwOV4DYWPK3LZwB7xuAteRP53gz3qFAEnn4DSGUcDbNOGrDimryEuFO8Vh9Yve6TJJLCtasSlhcAVCWKoJtxYcK6XNiSMlDzBhYDysS4Y48Hmz-gKWl1OZANs2hvwEG0BUkMJgZX6utQroTm8p54hZEDSmk4fDacvQ3uH17hE_ent2VQ"

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