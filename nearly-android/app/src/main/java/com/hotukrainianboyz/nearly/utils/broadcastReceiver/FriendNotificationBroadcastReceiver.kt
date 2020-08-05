package com.hotukrainianboyz.nearly.utils.broadcastReceiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.hotukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import com.hotukrainianboyz.nearly.utils.worker.RespondToFriendRequestWorker

class FriendNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val toAccept = intent.getBooleanExtra(FirebaseMessagingService.ACCEPT, false)
        val senderId = intent.getStringExtra(FirebaseMessagingService.SENDER)

        senderId?.let {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                FirebaseMessagingService.SENDER to senderId,
                FirebaseMessagingService.ACCEPT to toAccept
            )
            val request = OneTimeWorkRequestBuilder<RespondToFriendRequestWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(senderId.hashCode())

        } ?: Log.e(TAG, "Illegal receive without senderId")

    }

    companion object {
        private const val TAG = "FriendNotificationBroadcastReceiver"

        fun makeIntent(context: Context, senderId: String, toAccept: Boolean): Intent{
            val intent = Intent(context,FriendNotificationBroadcastReceiver::class.java)
            intent.putExtra(FirebaseMessagingService.SENDER, senderId)
            intent.putExtra(FirebaseMessagingService.ACCEPT, toAccept)
            return intent
        }
    }

}