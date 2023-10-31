package com.sekalisubmit.storymu.data.local.room.story

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(story: List<Story>)

    @Query("DELETE FROM story")
    fun delete()

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getStoryData(): LiveData<List<Story>>
}