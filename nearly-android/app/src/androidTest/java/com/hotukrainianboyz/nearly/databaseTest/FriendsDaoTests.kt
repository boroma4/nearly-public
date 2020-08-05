package com.hotukrainianboyz.nearly.databaseTest

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hotukrainianboyz.nearly.dao.FriendsDao
import com.hotukrainianboyz.nearly.database.AppRoomDatabase
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.hotukrainianboyz.nearly.utils.observer.observeOnce
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.IOException

class FriendsDaoTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var friendsDao: FriendsDao
    private lateinit var db: AppRoomDatabase
    private val testFriend = Friend("test-id", "Boggo",  "",null,UserStatus.ONLINE,FriendRequestStatus.ACCEPTED)
    private val testFriend2 = Friend("test-id2", "Boggo", "", "kek",UserStatus.BUSY,FriendRequestStatus.ACCEPTED)
    private val testFriend3 = Friend("test-id2", "Boggo", "", "kek",UserStatus.OFFLINE,FriendRequestStatus.ACCEPTED)

    //before each test
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppRoomDatabase::class.java
        ).build()
        friendsDao = db.friendsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insert_and_update_are_working_correctly() = runBlocking  {
        friendsDao.getFriends().observeOnce { assertTrue("Database was not empty after initialization!", it.isEmpty()) }
        assertNull(friendsDao.getFriendById(testFriend.id))
        friendsDao.insertFriend(testFriend)
        assertEquals(testFriend,friendsDao.getFriendById(testFriend.id))
        friendsDao.getFriends().observeOnce { assertTrue(it.size == 1) }
        friendsDao.insertFriend(testFriend2)
        friendsDao.getFriends().observeOnce { assertTrue(it.size == 2) }
        friendsDao.insertFriend(testFriend3)
        friendsDao.getFriends().observeOnce { assertTrue("Insert with the same id failed",it.size == 2) }
        friendsDao.getFriends().observeOnce {assertFalse("Insert updated data!!",it[1].userStatus == UserStatus.OFFLINE) }
        friendsDao.updateFriend(testFriend3)
        friendsDao.getFriends().observeOnce { assertTrue("Update did not happen!!",it[1].userStatus == UserStatus.OFFLINE) }
    }

    @Test
    fun delete_is_working_correctly() = runBlocking{
        friendsDao.insertFriend(testFriend)
        friendsDao.insertFriend(testFriend2)
        friendsDao.deleteFriend(testFriend2)
        friendsDao.getFriends().observeOnce { assertTrue(it.size == 1) }
        friendsDao.getFriends().observeOnce { assertTrue("Wrong user was deleted!!",it[0].id == testFriend.id) }
        friendsDao.insertFriend(testFriend2)
        friendsDao.deleteAll()
        friendsDao.getFriends().observeOnce { assertTrue("Delete all did not remove all !!",it.isEmpty()) }
    }

}

