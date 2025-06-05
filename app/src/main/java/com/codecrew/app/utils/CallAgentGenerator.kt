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

        /*callAgent.addOnIncomingCallListener(object: IncomingCallListener {
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
        })*/

        return callAgent
    }

    companion object {
        const val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kZTZmLTNlMTMtY2U0Ni0wNGJkNDU2MGY0NGYiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkxMDExNTYiLCJleHAiOjE3NDkxODc1NTYsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQsdm9pcCIsInJlc291cmNlSWQiOiJhNjdmZjA3MC0yYzgyLTQwYTktYTY3Ny0yZDNjMTZlNzJiZTEiLCJyZXNvdXJjZUxvY2F0aW9uIjoidW5pdGVkc3RhdGVzIiwiaWF0IjoxNzQ5MTAxMTU2fQ.tDsY53KoPy1KiqIXJXDOXpkXWf1wDZ0mmS9iTDzFlbLgnDaUAaUCKV0DDQ3uUVo7WJe7goXhp7fTm4TmhmqGX3w9N3HRP31o22-ExaDfsc4yZoqqQBmSqZ_sESfRW4IIJsMoSPj-L-ezGK6OzMAcnHZ8LhDO1WTgdcwD5zUO2n4VZtb9uL_a52CHyjWJe0WmE2aMFsL_M4rgYZC-D75AH-3HmFbZVAfTNIhbODkH_jfkkLzlfX0YjIe04LKpvYCRB5QLJD9XOAZ0v2zPkZQd4pUJe59r82f_FhWI4nAfRDWe25ZMGoEysUwPs2U_dIOeouZKxhkz_EHVfClPFp5dDQ"

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