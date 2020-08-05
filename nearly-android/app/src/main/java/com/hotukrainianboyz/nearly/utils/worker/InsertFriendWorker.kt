package com.hotukrainianboyz.nearly.utils.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hotukrainianboyz.nearly.database.AppRoomDatabase
import com.hotukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InsertFriendWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        GlobalScope.launch(Dispatchers.Default) {
            val dao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
            val requester = JsonUtils.jsonToFriend(inputData.getString(FirebaseMessagingService.SENDER)!!)
            dao.insertFriend(requester)
        }
        return Result.success()
    }

}