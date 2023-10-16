package com.sekalisubmit.storymu.data.local.room.register

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "register")
@Parcelize
class Register(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "password")
    var password: String? = null,

    @ColumnInfo(name = "username")
    var username: String? = null
): Parcelable