package com.ukrainianboyz.nearly.model

import com.ukrainianboyz.nearly.entity.Friend
import com.ukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.ukrainianboyz.nearly.utils.apiUtils.UserStatus

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