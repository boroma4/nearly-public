package com.hotukrainianboyz.nearly.repository

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.hotukrainianboyz.nearly.dao.FriendsDao
import com.hotukrainianboyz.nearly.dao.UserDao
import com.hotukrainianboyz.nearly.entity.User
import com.hotukrainianboyz.nearly.service.webAPI.IBackendService
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.ApplicationPrefs
import com.hotukrainianboyz.nearly.dto.UpdateAppUserIdDto
import com.hotukrainianboyz.nearly.dto.toUser

class UserRepository(private val friendsDao: FriendsDao, private val userDao: UserDao, private val backendService: IBackendService) {

    //on logout
    suspend fun logout() {
       val user = userDao.getUser()
        user?.id?.let { FirebaseMessaging.getInstance().unsubscribeFromTopic(it) }
        userDao.removeUser()
        friendsDao.deleteAll()
    }
    // will use backend service or websocket con to notify others
    // to be used when need to update user (image, name etc)
    fun putUser(user: User){
        userDao.putUser(user)
    }

    //to use with google/facebook etc
    //throws exception if request failed, view model takes care of analyzing it
    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(Exception::class)
    suspend fun login(googleToken: String) {
        Log.d(TAG, googleToken)
        val user = backendService.login(googleToken).toUser()
        Log.d(TAG,"got user $user")
        Tasks.await(FirebaseMessaging.getInstance().subscribeToTopic(user.id)) //login should fail if subscription fails
        Log.d(TAG,"Subscribed to firebase notifications")
        userDao.putUser(user)
    }


    @Throws(Exception::class)
    suspend fun updateAppUserId(appUserId: String) {
        val user = userDao.getUser()
        backendService.updateAppUserId(UpdateAppUserIdDto(user!!.id, appUserId))
        user.appUserId = appUserId
        userDao.putUser(user)
    }

    companion object{
        const val TAG = "UserRepository"
    }
}