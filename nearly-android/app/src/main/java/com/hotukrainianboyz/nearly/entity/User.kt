package com.hotukrainianboyz.nearly.entity


data class User(
    val id: String,
    val name: String,
    val email: String,
    val userBio: String,
    val imageUrl: String?,
    var appUserId: String?
)