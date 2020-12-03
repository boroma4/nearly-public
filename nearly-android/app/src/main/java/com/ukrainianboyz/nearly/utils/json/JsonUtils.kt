package com.ukrainianboyz.nearly.utils.json

import com.google.gson.Gson
import com.ukrainianboyz.nearly.entity.Friend
import com.ukrainianboyz.nearly.entity.User
import com.ukrainianboyz.nearly.model.SecureUser

class JsonUtils {
    companion object{
        private val gson = Gson()

         fun jsonToUser(jsonString: String): User{
            return jsonToObject(jsonString,User::class.java)
        }

        fun jsonToFriend(jsonString: String): Friend{
            return jsonToObject(jsonString,Friend::class.java)
        }

        fun jsonToSecureUser(jsonString: String): SecureUser{
            return jsonToObject(jsonString, SecureUser::class.java)
        }

         fun toJson(objectClass: Any): String{
            return gson.toJson(objectClass)
        }

        private fun <T> jsonToObject(jsonString: String, objectClass:Class<T>): T{
            return gson.fromJson(jsonString,objectClass)
        }
    }
}