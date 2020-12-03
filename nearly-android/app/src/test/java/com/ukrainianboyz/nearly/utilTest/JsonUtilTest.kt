package com.ukrainianboyz.nearly.utilTest

import com.google.gson.Gson
import com.ukrainianboyz.nearly.entity.User
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import org.junit.Test
import org.junit.Assert.*

class JsonUtilTest {

    private val gson = Gson()
    private val testUser = User("test-id", "Boggo", "bmal.com", null)

    @Test
    fun json_util_converts_user_correctly(){
        val userJson = gson.toJson(testUser)
        val actualUser = JsonUtils.jsonToUser(userJson)
        val expectedUser = gson.fromJson(userJson,User::class.java)
        assertEquals("Output was not the same as Gson provides!",expectedUser,actualUser)
    }

    @Test
    fun to_json_works_correctly(){
        val expected = gson.toJson(testUser)
        val actual = JsonUtils.toJson(testUser)
        assertEquals("toJson result was not the same as Gson provides!",expected,actual)
    }

}