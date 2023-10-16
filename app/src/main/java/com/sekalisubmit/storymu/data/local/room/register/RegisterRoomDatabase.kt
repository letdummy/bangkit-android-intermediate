package com.sekalisubmit.storymu.data.local.room.register

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Register::class], version = 1, exportSchema = false)
abstract class RegisterRoomDatabase: RoomDatabase() {

    abstract fun registerDao(): RegisterDao

    companion object {
        @Volatile
        private var INSTANCE: RegisterRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): RegisterRoomDatabase {
            if (RegisterRoomDatabase.INSTANCE == null) {
                synchronized(RegisterRoomDatabase::class.java) {
                    RegisterRoomDatabase.INSTANCE = Room.databaseBuilder(context.applicationContext,
                        RegisterRoomDatabase::class.java, "login")
                        .build()
                }
            }
            return RegisterRoomDatabase.INSTANCE as RegisterRoomDatabase
        }
    }
}