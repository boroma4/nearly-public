package com.hotukrainianboyz.nearly.utils.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hotukrainianboyz.nearly.database.AppRoomDatabase
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.utils.webSocket.FRIEND_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChangeFriendStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        GlobalScope.launch(Dispatchers.Default) {
            val dao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
            val friendId = inputData.getString(FRIEND_ID)!!
            val friend = dao.getFriendById(friendId)
            friend?.let {
                val updated = Friend(
                    it.id, it.name, it.userBio, it.imageUrl,
                    UserStatus.fromInt(inputData.getInt(STATUS, 0)),
                    it.friendRequestStatus, it.appUserId
                )
                dao.updateFriend(updated)
            }
        }
        return Result.success()
    }

    companion object {
        const val STATUS = "Status"
    }

}