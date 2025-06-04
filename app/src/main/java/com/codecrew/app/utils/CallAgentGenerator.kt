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
        /*val callAgentOption = CallAgentOptions()
        val telecomeManagerOptions = TelecomManagerOptions(phoneID)
        callAgentOption.setTelecomManagerOptions(telecomeManagerOptions)*/

        val callClientOptions = CallClientOptions()
        val callClient = CallClient(callClientOptions)
        val communicationTokenCredential = CommunicationTokenCredential(userToken)
        //val callAgent = callClient.createCallAgent(context, communicationTokenCredential, callAgentOption).get()
        val callAgent = callClient.createCallAgent(context, communicationTokenCredential).get()

        callAgent.addOnIncomingCallListener(object: IncomingCallListener {
            override fun onIncomingCall(incomingCall: IncomingCall?) {
                Log.d("ACS", "onIncomingCall = ${incomingCall?.callerInfo?.displayName}")

                incomingCall?.addOnCallEndedListener(object: PropertyChangedListener{
                    override fun onPropertyChanged(args: PropertyChangedEvent?) {

                    }
                })
            }
        })

        return callAgent
    }

    companion object {
        private const val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkRCQTFENTczNEY1MzM4QkRENjRGNjA4NjE2QTQ5NzFCOTEwNjU5QjAiLCJ4NXQiOiIyNkhWYzA5VE9MM1dUMkNHRnFTWEc1RUdXYkEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOmE2N2ZmMDcwLTJjODItNDBhOS1hNjc3LTJkM2MxNmU3MmJlMV8wMDAwMDAyNy1kYTI5LTlmNzUtZDY4YS0wODQ4MjIwMDkwOTEiLCJzY3AiOjE3OTIsImNzaSI6IjE3NDkwMjk0ODUiLCJleHAiOjE3NDkxMTU4ODUsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQsdm9pcCIsInJlc291cmNlSWQiOiJhNjdmZjA3MC0yYzgyLTQwYTktYTY3Ny0yZDNjMTZlNzJiZTEiLCJyZXNvdXJjZUxvY2F0aW9uIjoidW5pdGVkc3RhdGVzIiwiaWF0IjoxNzQ5MDI5NDg1fQ.pJWoyllzJ2HIiTBLaoo5fP0dayJw-NFOVUuiMPbU_QjO5cLu3yGru4qmifxSDAwKi3vzkZ5YspFAEenYBZSsuiIdUqoNqnTOtQ4T1ng8qXhMeOK5LH_Fb_-WmmM3KGY-PZvlAGLAmqXwszYZfmg2HvEcmAMxZvXsfNADwYhdAs53yriQFNnaNEc591OMh8UumsrSwzC8iYPtja1oZ4OAM0wVchUXeaAvibXw3AwRdEBlF6s6ooUEyZvF19C4rr7cfTPGNHbRPzgpdC9CNrp3duYpaagz3tmXscWeywCtPq7cS-UOLU2s_KOYjY5RLhOVhXpZudp3AGWilOX6WfX9sw"
        //const val phoneID = "abcdefghijklmnopqrstuvwxyz"

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