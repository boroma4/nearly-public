package com.ukrainianboyz.nearly.utils.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.repository.FriendsRepository
import com.ukrainianboyz.nearly.service.firebase.FirebaseMessagingService
import com.ukrainianboyz.nearly.service.webAPI.BackendService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RespondToFriendRequestWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val senderId = inputData.getString(FirebaseMessagingService.SENDER)
        val toAccept = inputData.getBoolean(FirebaseMessagingService.ACCEPT, false)
        val dao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
        val backendService = BackendService().backendApi
        val repository = FriendsRepository(dao, backendService)

        try {
            // can run blocking bc work is done on separate thread by default
            GlobalScope.launch(Dispatchers.Default) {
                if (senderId != null) {
                    repository.respondToFriendRequest(senderId, toAccept)
                }else{
                    Log.e(TAG, " couldn't perform friend response")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, " couldn't perform friend response")
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "RespondToFriendRequestWorker"
    }

}