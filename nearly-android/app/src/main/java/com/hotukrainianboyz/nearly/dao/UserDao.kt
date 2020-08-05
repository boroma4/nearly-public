package com.hotukrainianboyz.nearly.dao

import android.content.SharedPreferences
import com.hotukrainianboyz.nearly.entity.User
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.ApplicationPrefs

class UserDao(private val preferences: SharedPreferences) {

    private val editor = preferences.edit()

    fun getUser(): User?{
        val userJson = preferences.getString(ApplicationPrefs.USER_KEY, "")
        return if(userJson.isNullOrBlank()) null else JsonUtils.jsonToUser(userJson)
    }

     fun putUser(user:User){
        val userJson = JsonUtils.toJson(user)
        editor.putString(ApplicationPrefs.USER_KEY,userJson)
        editor.commit()
    }
    fun removeUser(){
        editor.remove(ApplicationPrefs.USER_KEY)
        editor.commit()
    }

}