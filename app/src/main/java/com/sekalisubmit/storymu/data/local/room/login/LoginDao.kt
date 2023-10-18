package com.sekalisubmit.storymu.data.local.room.login

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface LoginDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(login: Login)

    @Update
    fun update(login: Login)

    @Query("DELETE FROM login")
    fun delete()

    @Query("SELECT * FROM login")
    fun getUserData(): LiveData<Login>

}