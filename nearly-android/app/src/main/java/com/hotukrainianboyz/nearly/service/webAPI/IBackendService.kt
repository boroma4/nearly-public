package com.hotukrainianboyz.nearly.service.webAPI

import com.hotukrainianboyz.nearly.dto.*
import com.hotukrainianboyz.nearly.model.SecureUser
import retrofit2.http.*


interface IBackendService {

    @GET("login/googleToken/{token}")
    suspend fun login(@Path ("token") token: String): UserDto

    @GET("friends/allFriends/{id}")
    suspend fun getFriends(@Path ("id") userId: String): List<SecureUser>

    @GET("friends/outgoingFriendRequests/{id}")
    suspend fun getOutgoingRequests(@Path ("id") userId: String): List<SecureUser>

    @GET("friends/incomingFriendRequests/{id}")
    suspend fun getIncomingRequests(@Path ("id") userId: String): List<SecureUser>

    @GET("friends/blockedByUser/{id}")
    suspend fun getBlockedUsers(@Path ("id") userId: String): List<SecureUser>

    @POST("friends/add")
    suspend fun addFriend(@Body body: RelationshipRequestDto)

    @POST("friends/removeFriend")
    suspend fun removeFriend(@Body body: RelationshipRequestDto)

    @POST("friends/respond")
    suspend fun respondToFriendRequest(@Body body: FriendResponseDto)

    @POST("friends/search")
    suspend fun getTopUsers(@Body body: SearchUserRequestDto): List<SecureUser>

    @POST("friends/blockUser")
    suspend fun blockUser(@Body body: RelationshipRequestDto)

    @POST("friends/unblockUser")
    suspend fun unblockUser(@Body body: RelationshipRequestDto)

    @POST("friends/removeAddRequest")
    suspend fun cancelFriendRequest(@Body body: RelationshipRequestDto)

    @POST("calls/command")
    suspend fun sendFirebaseCallCommand(@Body body: FirebaseCommandDto)

    @PUT("user/updateAppUserID")
    suspend fun updateAppUserId(@Body body: UpdateAppUserIdDto)
}