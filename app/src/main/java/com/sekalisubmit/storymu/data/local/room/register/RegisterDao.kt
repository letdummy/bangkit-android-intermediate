package com.sekalisubmit.storymu.data.local.room.register

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface RegisterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(register: Register)

    @Update
    fun update(register: Register)

    @Delete
    fun delete(register: Register)

    @Query("SELECT * FROM register WHERE email = :email")
    fun getUserToken(email: String): Register
}