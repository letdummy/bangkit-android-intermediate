package com.sekalisubmit.storymu.data.local.room.story

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "story")
@Parcelize
class Story(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: String,

    @ColumnInfo(name = "photoUrl")
    var photoUrl: String? = null,

    @ColumnInfo(name = "createdAt")
    var createdAt: String? = null,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "lon")
    var lon: Double? = null,

    @ColumnInfo(name = "lat")
    var lat: Double? = null
) : Parcelable