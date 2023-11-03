package com.sekalisubmit.storymu.data.repository

import android.app.Application
import com.sekalisubmit.storymu.data.local.room.story.Story
import com.sekalisubmit.storymu.data.local.room.story.StoryDao
import com.sekalisubmit.storymu.data.local.room.story.StoryRoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StoryRepository(application: Application) {

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val storyDao: StoryDao

    init {
        val db = StoryRoomDatabase.getInstance(application)
        storyDao = db.storyDao()
    }

    fun insert(story: List<Story>) {
        executorService.execute {
            storyDao.insert(story)
        }
    }

    fun getStories() = storyDao.getStoryData()

    fun getStoriesWithLocation() = storyDao.getStoryWithLocation()

}