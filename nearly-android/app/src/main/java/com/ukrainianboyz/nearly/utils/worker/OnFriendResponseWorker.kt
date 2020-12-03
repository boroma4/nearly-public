package com.ukrainianboyz.nearly.utils.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import com.ukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OnFriendResponseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val senderId = inputData.getString(FirebaseMessagingService.SENDER)
        val accept = inputData.getBoolean(FirebaseMessagingService.ACCEPT,false)
        GlobalScope.launch(Dispatchers.Default) {
            val dao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
            val newFriend = dao.getFriendById(senderId!!)
            newFriend?.let {
                if (accept) {
                    newFriend.friendRequestStatus = FriendRequestStatus.ACCEPTED
                    dao.updateFriend(newFriend)
                } else {
                    dao.deleteFriend(newFriend)
                }
            }
        }
        return Result.success()
    }

}