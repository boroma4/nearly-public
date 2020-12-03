package com.ukrainianboyz.nearly.utils.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeleteFromFriendsTableWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        GlobalScope.launch(Dispatchers.Default) {
            val dao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
            val requester = dao.getFriendById(inputData.getString(FirebaseMessagingService.SENDER)!!)
            requester?.let { dao.deleteFriend(requester) }
        }
        return Result.success()
    }

}