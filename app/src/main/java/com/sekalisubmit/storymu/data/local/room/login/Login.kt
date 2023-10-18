package com.sekalisubmit.storymu.data.local.room.login

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "login")
@Parcelize
class Login(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "token")
    var token: String? = null
) : Parcelable