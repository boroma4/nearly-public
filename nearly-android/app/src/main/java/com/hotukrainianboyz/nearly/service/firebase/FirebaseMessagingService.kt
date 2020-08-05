package com.hotukrainianboyz.nearly.service.firebase

import android.util.Log
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hotukrainianboyz.nearly.ApplicationPrefs
import com.hotukrainianboyz.nearly.activity.CallNotificationActivity
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.service.notifications.NotificationService
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.utils.webSocket.DISMISS
import com.hotukrainianboyz.nearly.utils.webSocket.INIT_BACKEND_COMPAT
import com.hotukrainianboyz.nearly.utils.worker.DeleteFromFriendsTableWorker
import com.hotukrainianboyz.nearly.utils.worker.InsertFriendWorker
import com.hotukrainianboyz.nearly.utils.worker.OnFriendResponseWorker
import com.hotukrainianboyz.nearly.utils.worker.WorkerUtil
import com.hotukrainianboyz.nearly.viewModel.CallNotificationViewModel

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        // Check if message contains a data payload.
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + message.data)
            val senderId = message.data[SENDER]
            val command = message.data[COMMAND]
            senderId?.let {
                command?.let {
                    when (it) {
                        INIT_BACKEND_COMPAT -> {
                            if(CallNotificationViewModel.placeCall(senderId)) {
                                startActivity(
                                    CallNotificationActivity.makeIntent(
                                        this,
                                        senderId,
                                        ApplicationPrefs.isInForeground
                                    )
                                )
                            }
                        }
                        DISMISS -> {
                            CallNotificationViewModel.removeCall(senderId)
                        }
                        FRIEND_REQUEST -> {
                            val imageUrl = message.data[PICTURE]
                            val bio = message.data[BIO]
                            val name = message.data[NAME]
                            val appUserId = message.data[APP_USER_ID]
                            NotificationService.showFriendRequestNotification(
                                this,
                                name!!,
                                senderId,
                                imageUrl!!
                            )
                            val requester = Friend(
                                senderId,
                                name,
                                bio!!,
                                imageUrl,
                                UserStatus.OFFLINE,
                                FriendRequestStatus.INCOMING,
                                appUserId
                            )
                            val data = workDataOf(SENDER to JsonUtils.toJson(requester))
                            WorkerUtil.enqueueOneTimeWork<InsertFriendWorker>(this, data)
                        }
                        FRIEND_RESPONSE -> {
                            val isAccepted = message.data[ACCEPT]!!.toBoolean()
                            val data = workDataOf(SENDER to senderId, ACCEPT to isAccepted)
                            WorkerUtil.enqueueOneTimeWork<OnFriendResponseWorker>(this, data)
                        }
                        REVOKE_FRIEND_REQUEST, FRIEND_REMOVED -> {
                            val data = workDataOf(SENDER to message.data[SENDER])
                            WorkerUtil.enqueueOneTimeWork<DeleteFromFriendsTableWorker>(this, data)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACCEPT = "accept"
        const val SENDER = "sender"
        private const val TAG = "FirebaseMessagingService"
        private const val FRIEND_REQUEST = "friendRequest"
        private const val FRIEND_RESPONSE = "friendResponse"
        private const val REVOKE_FRIEND_REQUEST = "revokeFriendRequest"
        private const val FRIEND_REMOVED = "friendRemoved"
        private const val COMMAND = "command"
        private const val PICTURE = "picture"
        private const val NAME = "name"
        private const val BIO = "bio"
        private const val APP_USER_ID = "appUserId"
    }
}