package com.hotukrainianboyz.nearly.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hotukrainianboyz.nearly.entity.Friend

@Dao
interface FriendsDao {

    @Query("SELECT * FROM friends_table WHERE id=:id ")
    suspend fun getFriendById(id: String): Friend?

    @Query("SELECT * FROM friends_table")
    fun getFriends(): LiveData<List<Friend>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFriend(friend: Friend): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: Iterable<Friend>)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("DELETE FROM friends_table")
    suspend fun deleteAll()

    @Update
    suspend fun updateFriend(friend: Friend)
}