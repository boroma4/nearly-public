package com.hotukrainianboyz.nearly.utils.dbConverter

import androidx.room.TypeConverter
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus

class EnumConverters {

    @TypeConverter
    fun toUserStatus(value: Int) = enumValues<UserStatus>()[value]

    @TypeConverter
    fun fromUserStatus(value: UserStatus) = value.ordinal

    @TypeConverter
    fun toFriendRequestStatus(value: Int) = enumValues<FriendRequestStatus>()[value]

    @TypeConverter
    fun fromFriendRequestStatus(value: FriendRequestStatus) = value.ordinal
}