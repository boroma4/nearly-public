package com.hotukrainianboyz.nearly.model

import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus

data class SecureUser (
    val userId: String,
    val userName: String,
    val userBio: String,
    val imageUrl: String?,
    val statusIndicator: Int,
    var appUserId: String
)


fun SecureUser.toFriend(requestStatus: FriendRequestStatus): Friend{
    return Friend(userId,userName,userBio,imageUrl, UserStatus.fromInt(statusIndicator),requestStatus, appUserId)
}