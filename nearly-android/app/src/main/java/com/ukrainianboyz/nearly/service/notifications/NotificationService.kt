package com.ukrainianboyz.nearly.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.utils.broadcastReceiver.FriendNotificationBroadcastReceiver
import com.ukrainianboyz.nearly.utils.image.ImageUtils
import java.util.*


class NotificationService {
    companion object {
        private const val TAG = "NotificationService"
        private lateinit var notificationManager: NotificationManager

        fun startService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                val name = context.getString(R.string.app_name)
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(
                    context.getString(R.string.default_notification_channel_id),
                    name,
                    importance
                )
                mChannel.description = descriptionText
                mChannel.enableLights(true)
                mChannel.enableVibration(true)
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }
        }

        fun showFriendRequestNotification(
            context: Context,
            userName: String,
            senderId: String,
            imageUrl: String
        ) {

            val acceptBroadcast = buildResponseBroadcastPendingIntent(context, senderId, true)
            val declineBroadcast = buildResponseBroadcastPendingIntent(context, senderId, false)

            val builder = NotificationCompat.Builder(
                context,
                context.getString(R.string.default_notification_channel_id)
            )
                .setSmallIcon(R.drawable.ic_feedback_black_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("$userName wants to add u!")
                .setSmallIcon(R.drawable.ic_app_name)
                .setLargeIcon(ImageUtils.loadBitmapFromUrl(context, imageUrl))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(
                    R.drawable.ic_baseline_done_outline_24,
                    context.getString(R.string.accept),
                    acceptBroadcast
                )
                .addAction(
                    R.drawable.ic_outline_close_24,
                    context.getString(R.string.decline),
                    declineBroadcast
                )

            Log.d(TAG, "Adding notification ${senderId.hashCode()}")
            notificationManager.notify(senderId.hashCode(), builder.build())
        }

        private fun buildResponseBroadcastPendingIntent(
            context: Context,
            senderId: String,
            toAccept: Boolean
        ): PendingIntent {

            val intent = FriendNotificationBroadcastReceiver.makeIntent(context,senderId,toAccept)

            return PendingIntent.getBroadcast(
                context, UUID.randomUUID().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}