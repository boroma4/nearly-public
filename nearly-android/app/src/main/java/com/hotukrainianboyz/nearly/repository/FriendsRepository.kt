package com.hotukrainianboyz.nearly.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.hotukrainianboyz.nearly.dao.FriendsDao
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.service.webAPI.IBackendService
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.ApplicationPrefs
import com.hotukrainianboyz.nearly.dto.*
import com.hotukrainianboyz.nearly.model.SecureUser
import com.hotukrainianboyz.nearly.model.toFriend
import com.hotukrainianboyz.nearly.utils.webSocket.DISMISS
import com.hotukrainianboyz.nearly.utils.webSocket.DISMISS_SELF
import com.hotukrainianboyz.nearly.utils.webSocket.INIT_BACKEND_COMPAT
import kotlinx.coroutines.*

class FriendsRepository(
    private val friendsDao: FriendsDao,
    private val backendService: IBackendService
) {

    val friends: LiveData<List<Friend>> = friendsDao.getFriends()
    private val userId: String

    init {
        val userJson = ApplicationPrefs.encryptedPrefs.getString(
            ApplicationPrefs.USER_KEY, ""
        )
        userId = JsonUtils.jsonToUser(userJson!!).id
    }

    @Throws(Exception::class)
    suspend fun sendFriendRequest(requestedUser: SecureUser) {
        //send request and insert friend with FriendRequestStatus "OUTGOING"
        backendService.addFriend(RelationshipRequestDto(userId, requestedUser.userId))
        friendsDao.insertFriend(requestedUser.toFriend(FriendRequestStatus.OUTGOING))
    }

    @Throws(Exception::class)
    suspend fun respondToFriendRequest(requesterId: String, accept: Boolean) {
        //send response and update friend with FriendRequestStatus "ACCEPTED" or remove
        val friendToChange = friendsDao.getFriendById(requesterId)
        friendToChange?.let {
            backendService.respondToFriendRequest(FriendResponseDto(userId, requesterId, accept))
            if (accept) {
                friendToChange.friendRequestStatus = FriendRequestStatus.ACCEPTED
                friendsDao.updateFriend(friendToChange)
            } else {
                friendsDao.deleteFriend(friendToChange)
            }
        } ?: throw IllegalArgumentException("Couldn't find friend")
    }

    @Throws(Exception::class)
    suspend fun searchTopUsersByEmail(letters: String, skipPages: Int): List<SecureUser> {
        return backendService.getTopUsers(SearchUserRequestDto(letters, skipPages, SEARCH_LOAD_AMOUNT))
    }

    @Throws(Exception::class)
    suspend fun fetchFriendsAndRequests() {
        Log.d(TAG, "fetching everything for $userId")
        val friends = GlobalScope.async(Dispatchers.IO) {
            backendService.getFriends(userId)
        }
        val incoming = GlobalScope.async(Dispatchers.IO) {
            backendService.getIncomingRequests(userId)
        }
        val outgoing = GlobalScope.async(Dispatchers.IO) {
            backendService.getOutgoingRequests(userId)
        }
        val blockedUsers = GlobalScope.async(Dispatchers.IO){
            backendService.getBlockedUsers(userId)
        }

        val result = awaitAll(friends, incoming, outgoing, blockedUsers)
        val finalList = ArrayList<Friend>(result[0].size + result[1].size + result[2].size + result[3].size)

        for (user in result[0]) finalList.add(user.toFriend(FriendRequestStatus.ACCEPTED))
        for (user in result[1]) finalList.add(user.toFriend(FriendRequestStatus.INCOMING))
        for (user in result[2]) finalList.add(user.toFriend(FriendRequestStatus.OUTGOING))
        for (user in result[3]) finalList.add(user.toFriend(FriendRequestStatus.BLOCKED))

        friendsDao.deleteAll() // if network requests succeeded, this is a safe thing to do
        friendsDao.insertFriends(finalList)
        Log.d(TAG, "finished fetching")
    }

    @Throws(Exception::class)
    suspend fun fetchIncomingRequests() {
        Log.d(TAG, "fetching incoming requests for $userId")
        val requestJob = GlobalScope.async(Dispatchers.IO) {
            backendService.getIncomingRequests(userId)
        }
        val incomingRequests = requestJob.await().map{ it.toFriend(FriendRequestStatus.INCOMING) }

        friendsDao.insertFriends(incomingRequests)
        Log.d(TAG, "finished fetching incoming requests")
    }

    @Throws(Exception::class)
    suspend fun fetchFriendsOnly() {
        Log.d(TAG, "fetching friends for $userId")
        val requestJob = GlobalScope.async(Dispatchers.IO) {
            backendService.getFriends(userId)
        }
        val friends = requestJob.await().map{ it.toFriend(FriendRequestStatus.ACCEPTED) }

        friendsDao.insertFriends(friends)
        Log.d(TAG, "finished fetching friends")
    }

    @Throws(Exception::class)
    suspend fun fetchBlockedUsers() {
        Log.d(TAG, "fetching blocked users for $userId")
        val requestJob = GlobalScope.async(Dispatchers.IO) {
            backendService.getBlockedUsers(userId)
        }
        val friends = requestJob.await().map{ it.toFriend(FriendRequestStatus.BLOCKED) }

        friendsDao.insertFriends(friends)
        Log.d(TAG, "finished fetching blocked users")
    }

    @Throws(Exception::class)
    suspend fun sendCallCommand(receiverId: String, command: String) {
        val isUrgent = command == INIT_BACKEND_COMPAT
        // swapping users in case of dismiss, where the same user is supposed to receive the message
        var receiver = receiverId
        var sender = userId
        var cmd = command
        //swap ids
        if (command == DISMISS_SELF) {
            receiver = userId
            sender = receiverId
            cmd = DISMISS
        }
        val payload = FirebaseCommandDto(
            receiver, sender,
            cmd, isUrgent
        )
        Log.d(TAG,payload.toString())
        backendService.sendFirebaseCallCommand(payload)
    }

    @Throws(Exception::class)
    suspend fun cancelOutgoingFriendRequest(friend: Friend) {
        backendService.cancelFriendRequest(RelationshipRequestDto(userId,friend.id))
        friendsDao.deleteFriend(friend)
    }

    @Throws(Exception::class)
    suspend fun deleteFriend(friend: Friend) {
        backendService.removeFriend(RelationshipRequestDto(userId, friend.id))
        friendsDao.deleteFriend(friend)
    }

    @Throws(Exception::class)
    suspend fun blockUser(friend: Friend) {
        backendService.blockUser(RelationshipRequestDto(userId, friend.id))
        friend.friendRequestStatus = FriendRequestStatus.BLOCKED
        friendsDao.updateFriend(friend)
    }

    @Throws(Exception::class)
    suspend fun unblockUser(friend: Friend) {
        backendService.unblockUser(RelationshipRequestDto(userId, friend.id))
        friendsDao.deleteFriend(friend)
    }

    /**
     * Overloads for blocking from Search
     */
    @Throws(Exception::class)
    suspend fun blockUser(user: SecureUser) {
        backendService.blockUser(RelationshipRequestDto(userId, user.userId))
        friendsDao.insertFriend(user.toFriend(FriendRequestStatus.BLOCKED))
    }

    companion object {
        private const val TAG = "FriendsRepo"
        const val SEARCH_LOAD_AMOUNT = 5
    }
}