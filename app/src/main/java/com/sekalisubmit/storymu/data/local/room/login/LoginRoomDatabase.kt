package com.sekalisubmit.storymu.data.local.room.login

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Login::class], version = 1, exportSchema = false)
abstract class LoginRoomDatabase: RoomDatabase(){

    abstract fun loginDao(): LoginDao

    companion object {
        @Volatile
        private var INSTANCE: LoginRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): LoginRoomDatabase {
            if (INSTANCE == null) {
                synchronized(LoginRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        LoginRoomDatabase::class.java, "login")
                        .build()
                }
            }
            return INSTANCE as LoginRoomDatabase
        }
    }
}