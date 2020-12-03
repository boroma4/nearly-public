package com.ukrainianboyz.nearly.service.signalR

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.workDataOf
import com.ukrainianboyz.nearly.ApplicationPrefs
import com.ukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.ukrainianboyz.nearly.utils.webSocket.*
import com.ukrainianboyz.nearly.utils.worker.ChangeFriendStatusWorker
import com.ukrainianboyz.nearly.utils.worker.WorkerUtil
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.Single
import kotlinx.coroutines.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.*


class SignalRClient {
    companion object {
        private const val RETRY_DELAY_MS: Long = 5000
        private const val URL = " https://nearly.azurewebsites.net/hub"
        private const val RTC_MESSAGE = "RtcMessage"
        private const val RTC_ICE = "RtcIce"
        private const val RTC_SES = "RtcSes"
        private const val GENERAL_TAG = "SignalRGeneral"
        private const val CALL_TAG = "SignalRCall"
        private var calleeId: String = ""
        private var killConnection = false
        private var activityCallback: ISingaling? = null
        private lateinit var hubConnection: HubConnection
        private lateinit var userId: String
        private lateinit var context: Context

        // checked when application goes into background, if call is active, web socket connection is kept
        var onCall = false
            private set

        // to restrict access to some features if user is disconnected
        var isOnline: MutableLiveData<Boolean> = MutableLiveData(false)
            private set

        // to determine what to do in VideoCallActivity
        var initiatedCall = false
            private set

        // first time when this field will be seen, connection will be initializing
        var isConnecting = true
            private set

        @SuppressLint("CheckResult")
        suspend fun connectToHub(appContext: Context, userId: String) {
            context = appContext
            this.userId = userId
            //if instance is left no need to redefine callbacks
            if (this::hubConnection.isInitialized) {
                tryReconnect()
                return
            }
            hubConnection = HubConnectionBuilder.create(URL)
                .withAccessTokenProvider(Single.defer { Single.just(userId) })
                .build()

            hubConnection.on(RTC_ICE, { message: IceCandidateMessage ->
                Log.d(CALL_TAG, "Ice Received :: ${message.type}")
                if(onCall) activityCallback?.onIceCandidateReceived(message)
            }, IceCandidateMessage::class.java)

            hubConnection.on(RTC_SES, { message: SessionDescriptionMessage ->
                Log.d(CALL_TAG, "Session Received :: ${message.type}")
                if(onCall){
                    if (message.type.toLowerCase(Locale.ROOT) == "offer") activityCallback?.onOfferReceived(
                        message
                    )
                    else activityCallback?.onAnswerReceived(message)
                }
            }, SessionDescriptionMessage::class.java)

            hubConnection.on(RTC_MESSAGE, { message: CommandMessage ->
                Log.d(CALL_TAG, " Received :: ${message.type} ${message.command}")
                if (message.command.startsWith(HANGUP) && onCall) {
                    Log.d(CALL_TAG, "hanging up")
                        activityCallback?.onRemoteHangUp()
                        // if remote hung up while app was in background on an active call, it is safe to disconnect web socket
                        if(!ApplicationPrefs.isInForeground) disconnectFromHub()
                }
                if (message.command == HOLD && onCall) activityCallback?.onHoldReceived()
                if (message.command == RESUME && onCall) activityCallback?.onResumeReceived()
            }, CommandMessage::class.java)

            hubConnection.on(USER_ONLINE, {id: String ->
                Log.d(GENERAL_TAG,"user online")
                enqueueOneTimeStatusChangeWork(context,id,UserStatus.ONLINE)
            }, String::class.java)

            hubConnection.on(USER_OFFLINE, { id: String ->
                Log.d(GENERAL_TAG,"user offline")
                enqueueOneTimeStatusChangeWork(context,id,UserStatus.OFFLINE)
            }, String::class.java)

            hubConnection.on(USER_ON_CALL, { id: String ->
                enqueueOneTimeStatusChangeWork(context,id,UserStatus.BUSY)
            }, String::class.java)

            hubConnection.onClosed { ex ->
                ex?.message?.let { Log.e(GENERAL_TAG, it + killConnection) }
                if (!killConnection) {
                    isConnecting = true
                    Log.d(GENERAL_TAG, "reconnecting")
                    // global scope must be ok here, this connection is for the whole app
                    GlobalScope.launch(Dispatchers.IO) {
                        tryToConnect()
                    }
                }
                isOnline.postValue(false)
            }
            // connect after defining all the callbacks
            tryToConnect()
        }

        //safe version of tryToConnect
        suspend fun tryReconnect() {
            if (!this::hubConnection.isInitialized || userId.isBlank() || isOnline.value == true  || isConnecting) return
            tryToConnect()
        }

        /**
         * Function to be called asynchronously, tries to connect to SignalR hub until connection is established
         * and updates livedata connection state value
         */
        private suspend fun tryToConnect() {
            isConnecting = true
            while (true) {
                val connected = try {
                    Log.d(GENERAL_TAG, "Trying to connect to hub")
                    hubConnection.start().blockingAwait()
                    hubConnection.connectionState == HubConnectionState.CONNECTED
                } catch (e: Exception) {
                    Log.e(GENERAL_TAG, e.message!!)
                    false
                }
                isOnline.postValue(connected)
                if (!connected) delay(RETRY_DELAY_MS) else break
            }
            isConnecting = false
        }

        /**
         * Go Offline, reconnect will not be started
         */
        fun disconnectFromHub(removeUser: Boolean = false) {
            Log.d(GENERAL_TAG, "Disconnecting from hub")
            stopCall()
            killConnection = true
            if(removeUser) userId = ""  // should only use on logout
            if (this::hubConnection.isInitialized) {
                hubConnection.stop()
            }
        }

        /**
         *   ISignalling is implemented by video call activity to make sure start call cannot be called from elsewhere
         */
        fun startCall(
            callbackManager: ISingaling,
            calleeId: String,
            isInitiator: Boolean
        ): Boolean {
            if (hubConnection.connectionState != HubConnectionState.CONNECTED || onCall) return false
            onCall = true
            initiatedCall = isInitiator
            this.calleeId = calleeId
            activityCallback = callbackManager
            return true
        }

        /**
         *   Erase static current call data
         */
        fun stopCall() {
            if (!onCall) return
            sendHangup(calleeId)
            sendCallStatusUpdate(false)
            activityCallback = null
            calleeId = ""
            onCall = false
            initiatedCall = false
        }

        fun sendCallStatusUpdate(callStarted: Boolean){
            hubConnection.send(UPDATE_CALL_STATUS, callStarted, hubConnection.connectionId)
        }

        fun sendDescription(description: SessionDescription) =
            sendMessage(SessionDescriptionMessage(description))

        fun sendIceCandidate(iceCandidate: IceCandidate) =
            sendMessage(IceCandidateMessage(iceCandidate))

        fun sendHold() = sendMessage(CommandMessage(HOLD))

        fun sendResume() = sendMessage(CommandMessage(RESUME))

        fun sendHangup(receiverId: String) {
            if (hubConnection.connectionState != HubConnectionState.CONNECTED) return
            hubConnection.send(RTC_MESSAGE, receiverId, CommandMessage("$HANGUP${userId}"))
            Log.d(CALL_TAG, "Hangup message sent to $receiverId")
        }

        private fun sendMessage(message: Message) {
            // hangup is the only message that can be sent not from "call", and it has a separate method for it
            if (hubConnection.connectionState != HubConnectionState.CONNECTED || !onCall) return

            when (message) {
                is CommandMessage -> hubConnection.send(RTC_MESSAGE, calleeId, message)
                is IceCandidateMessage -> hubConnection.send(RTC_ICE, calleeId, message)
                is SessionDescriptionMessage -> hubConnection.send(RTC_SES, calleeId, message)
            }
        }

        private fun enqueueOneTimeStatusChangeWork(context: Context, id: String, status: UserStatus){
            WorkerUtil.enqueueOneTimeWork<ChangeFriendStatusWorker>(context,
                workDataOf(FRIEND_ID to id, ChangeFriendStatusWorker.STATUS to status.ordinal)
            )
        }
    }
}