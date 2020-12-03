package com.ukrainianboyz.nearly.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.entity.Friend
import com.ukrainianboyz.nearly.model.SecureUser
import com.ukrainianboyz.nearly.repository.FriendsRepository
import com.ukrainianboyz.nearly.service.webAPI.BackendService
import com.ukrainianboyz.nearly.service.webAPI.IBackendService
import com.ukrainianboyz.nearly.utils.apiUtils.NetworkRequestHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FriendsRepository
    private val backendService: IBackendService = BackendService().backendApi
    private val networkRequestHelper = NetworkRequestHelper(viewModelScope, TAG)
    private var searchPagesToSkip: Int = 0
    val friends: LiveData<List<Friend>>

    init {
        val friendsDao = AppRoomDatabase.getDatabase(application).friendsDao()
        repository = FriendsRepository(friendsDao, backendService)
        friends = repository.friends
    }

    fun sendFriendRequest(user: SecureUser, onFailed: () -> Unit = {}) {
        networkRequestHelper.process("sendFriendRequest",
            suspend{repository.sendFriendRequest(user)}, onFailed = onFailed)
    }

    fun respondToFriendRequest(requesterId: String, accept: Boolean, onFailed: () -> Unit = {}) {
        networkRequestHelper.process("respondToFriendRequest",
            suspend{ repository.respondToFriendRequest(requesterId, accept) }, onFailed = onFailed)
    }

    fun sendCallCommand(receiverId: String, command: String, onFailed: () -> Unit = {}){
        networkRequestHelper.process("sendCallCommand",
            suspend{repository.sendCallCommand(receiverId,command)}, onFailed = onFailed)
    }

    // ui will call await and handle errors
    // will return list of users or empty list on error
    fun getTopUsersAsync(letters: String, takeFirst: Boolean): Deferred<List<SecureUser>> {
        // start from the beginning or from the last value + step
        if(takeFirst) searchPagesToSkip = 0
        return viewModelScope.async(Dispatchers.IO) {
            repository.searchTopUsersByEmail(letters, searchPagesToSkip++)
        }
    }

    // only done on login or on refresh
    fun fetchFriendsAndRequests(onSucceeded: ()-> Unit = {},onFailed: () -> Unit = {}) {
        networkRequestHelper.process("fetchFriendsAndRequests",
            suspend{ repository.fetchFriendsAndRequests()}, onSucceeded = onSucceeded ,onFailed = onFailed)
    }

    // to use on refresh
    fun fetchIncomingRequests(onSucceeded: ()-> Unit = {}, onFailed: () -> Unit = {}) {
        networkRequestHelper.process("fetchIncomingRequests",
            suspend{ repository.fetchIncomingRequests() }, onSucceeded = onSucceeded ,onFailed = onFailed)
    }

    // to use when main activity is resumed
    fun fetchFriendsOnly(onSucceeded: ()-> Unit = {}, onFailed: () -> Unit = {}) {
        networkRequestHelper.process("fetchFriendsOnly",
            suspend{ repository.fetchFriendsOnly() }, onSucceeded = onSucceeded ,onFailed = onFailed)
    }

    // to use when list of blocked users is refreshed
    fun fetchBlockedUsers(onSucceeded: ()-> Unit = {}, onFailed: () -> Unit = {}) {
        networkRequestHelper.process("fetchBlockedUsers",
            suspend{ repository.fetchBlockedUsers() }, onSucceeded = onSucceeded ,onFailed = onFailed)
    }

    fun cancelOutgoingFriendRequest(friend: Friend, onFailed: () -> Unit = {}){
        networkRequestHelper.process("cancelOutgoingFriendRequest",
            suspend{ repository.cancelOutgoingFriendRequest(friend)}, onFailed = onFailed)
    }

    fun deleteFriend(friend: Friend, onFailed: () -> Unit = {}, onSucceeded: () -> Unit = {}){
        networkRequestHelper.process("deleteFriend",
            suspend{ repository.deleteFriend(friend)}, onFailed = onFailed, onSucceeded = onSucceeded)
    }

    fun blockUser(friend: Friend, onFailed: () -> Unit = {}, onSucceeded: () -> Unit = {}){
        networkRequestHelper.process("blockUser",
            suspend{repository.blockUser(friend)}, onFailed = onFailed, onSucceeded = onSucceeded)
    }

    fun blockUser(user: SecureUser, onFailed: () -> Unit = {}){
        networkRequestHelper.process("blockUser",
            suspend{repository.blockUser(user)}, onFailed = onFailed)
    }

    fun unblockUser(friend: Friend, onFailed: () -> Unit = {}){
        networkRequestHelper.process("unblockUser",
            suspend{ repository.unblockUser(friend)}, onFailed = onFailed)
    }


    companion object {
        private const val TAG = "FriendsViewModel"
    }
}