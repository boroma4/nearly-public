package com.ukrainianboyz.nearly.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.ukrainianboyz.nearly.dao.FriendsDao
import com.ukrainianboyz.nearly.dao.UserDao
import com.ukrainianboyz.nearly.database.AppRoomDatabase
import com.ukrainianboyz.nearly.dto.UserDto
import com.ukrainianboyz.nearly.dto.toUser
import com.ukrainianboyz.nearly.service.webAPI.IBackendService
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import com.ukrainianboyz.nearly.utils.observer.observeOnce
import com.ukrainianboyz.nearly.ApplicationPrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule  val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val fakeUserDto = UserDto("test-id", "Boggo", "bmal.com", "",null)
    private val fakeUser = fakeUserDto.toUser()
    private val fakeToken = "aaa-2-3-a"

    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository: UserRepository
    private lateinit var backendService: IBackendService
    private lateinit var userDao: UserDao
    private lateinit var friendsDao: FriendsDao

    //before each test
    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ApplicationPrefs.initEncryptedPrefs(context)
        prefs = ApplicationPrefs.encryptedPrefs
        backendService = mock(IBackendService::class.java)
        userDao = UserDao(prefs)
        friendsDao = AppRoomDatabase.getDatabase(context,GlobalScope).friendsDao()
        userRepository = UserRepository(friendsDao,userDao,backendService)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        userDao.removeUser()
    }

    @Test
    fun repository_works_correctly() = runBlocking{
        `when`(backendService.login(fakeToken)).thenReturn(fakeUserDto)
        userRepository.putUser(fakeUserDto.toUser())

        var userJson = prefs.getString(ApplicationPrefs.USER_KEY,"")
        assertNotNull("User was not saved !!!",userJson)
        var user = JsonUtils.jsonToUser(userJson!!)
        assertEquals("Obtained user was not the same as written one !!!",fakeUser,user)

        runBlocking {userRepository.logout()}
        assertEquals("User was not removed !!!","",prefs.getString(ApplicationPrefs.USER_KEY,""))
        friendsDao.getFriends().observeOnce {
            assertTrue("Friends were not deleted!!",it.isEmpty())
        }

        userRepository.login(fakeToken)
        userJson = prefs.getString(ApplicationPrefs.USER_KEY,"")
        assertNotNull("User was not saved after login !!!",userJson)
        user = JsonUtils.jsonToUser(userJson!!)
        assertEquals("Logged in user was not the same as expected !!!",fakeUser,user)
    }

    @Test(expected = HttpException::class)
    fun repository_throws_if_login_failed() = runBlocking{
        `when`(backendService.login(fakeToken)).thenThrow(HttpException::class.java)
         userRepository.login(fakeToken)
    }
}