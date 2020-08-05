package com.hotukrainianboyz.nearly.dto

import com.hotukrainianboyz.nearly.entity.User

data class UserDto(val userId: String, val userName: String, val email: String, val userBio: String, val imageUrl: String?, val appUserId: String?)


fun UserDto.toUser(): User{
    return User(userId,userName,email,userBio,imageUrl,appUserId)
}
