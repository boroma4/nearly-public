package com.hotukrainianboyz.nearly.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus


@Entity(tableName = "friends_table")
data class Friend(
    @PrimaryKey val id: String,
    val name: String,
    val userBio: String,
    val imageUrl: String?,
    val userStatus: UserStatus,
    var friendRequestStatus: FriendRequestStatus,
    var appUserId: String?
)