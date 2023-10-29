package com.sekalisubmit.storymu.data.local.room.story

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Story::class], version = 1, exportSchema = false)
abstract class StoryRoomDatabase: RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object {
        @Volatile
        private var instance: StoryRoomDatabase? = null
        fun getInstance(context: Context): StoryRoomDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryRoomDatabase::class.java, "story"
                ).build()
            }
    }
}