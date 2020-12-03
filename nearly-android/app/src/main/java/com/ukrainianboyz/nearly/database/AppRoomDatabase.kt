package com.ukrainianboyz.nearly.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ukrainianboyz.nearly.dao.FriendsDao
import com.ukrainianboyz.nearly.entity.Friend
import com.ukrainianboyz.nearly.utils.dbConverter.EnumConverters

// Annotates class to be a Room Database with a table (entity) of the User class
@Database(entities = [Friend:: class], version = 1, exportSchema = false)
@TypeConverters(EnumConverters::class)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun friendsDao(): FriendsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}