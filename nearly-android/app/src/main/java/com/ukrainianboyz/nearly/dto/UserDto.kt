package com.ukrainianboyz.nearly.dto

import com.ukrainianboyz.nearly.entity.User

data class UserDto(val userId: String, val userName: String, val email: String, val userBio: String, val imageUrl: String?, val appUserId: String?)


fun UserDto.toUser(): User{
    return User(userId,userName,email,userBio,imageUrl,appUserId)
}
