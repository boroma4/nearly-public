package com.hotukrainianboyz.nearly.utils.json

import com.google.gson.Gson
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.entity.User
import com.hotukrainianboyz.nearly.model.SecureUser
import com.hotukrainianboyz.nearly.utils.webSocket.IceCandidateMessage
import com.hotukrainianboyz.nearly.utils.webSocket.SessionDescriptionMessage

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