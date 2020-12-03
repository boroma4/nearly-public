package com.ukrainianboyz.nearly.dto

data class FirebaseCommandDto (val receiverId: String, val senderId: String, val command: String, val isUrgent: Boolean)