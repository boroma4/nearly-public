package com.hotukrainianboyz.nearly.utils.apiUtils

enum class UserStatus {
    OFFLINE,
    ONLINE,
    BUSY;

    companion object {
        fun fromInt(value: Int) = values().first { it.ordinal == value }
    }
}