package com.hotukrainianboyz.nearly.databaseTest

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.hotukrainianboyz.nearly.dao.UserDao
import com.hotukrainianboyz.nearly.entity.User
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.ApplicationPrefs
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class UserDaoTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var userDao: UserDao
    private lateinit var prefs: SharedPreferences
    private val testUser = User("test-id", "Boggo", "bmal.com", "",null)
    private val testUser2 = User("test-id", "Boggo", "al.com","", "kek")

      //before each test
        @Before
        fun init() {
          val context = ApplicationProvider.getApplicationContext<Context>()
          ApplicationPrefs.initEncryptedPrefs(context)
          prefs = ApplicationPrefs.encryptedPrefs
          userDao = UserDao(prefs)
        }

        @After
        fun cleanup(){
            userDao.removeUser()
        }

    @Test
     fun prefs_are_working_correctly() = runBlocking  {
        userDao.putUser(testUser)

        var userJson = prefs.getString(ApplicationPrefs.USER_KEY,"")
        assertNotNull("User was not saved !!!",userJson)
        var user = JsonUtils.jsonToUser(userJson!!)
        assertEquals("Obtained user was not the same as written one !!!",testUser,user)

        userDao.removeUser()
        assertEquals("User was not removed !!!","",prefs.getString(ApplicationPrefs.USER_KEY,""))

        userDao.putUser(testUser)
        userDao.putUser(testUser2)
        userJson = prefs.getString(ApplicationPrefs.USER_KEY,"")
        user = JsonUtils.jsonToUser(userJson!!)
        assertEquals("User did not get updated !!!",testUser2,user)
    }

}

