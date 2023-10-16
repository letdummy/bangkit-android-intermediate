package com.sekalisubmit.storymu.data.local.room.login

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    fun delete(login: Login)

    @Query("SELECT * FROM login WHERE email = :email")
    fun getUserToken(email: String): LiveData<Login>
}