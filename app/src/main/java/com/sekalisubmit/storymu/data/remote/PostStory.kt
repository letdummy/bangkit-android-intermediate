package com.sekalisubmit.storymu.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class PostStory(
    val description: String,
    val photo: MultipartBody.Part,
    val lon: Double,
    val lat: Double
) {
    fun getDescriptionRequestBody(): RequestBody {
        return description.toRequestBody("text/plain".toMediaType())
    }

    fun getLonRequestBody(): RequestBody {
        return lon.toString().toRequestBody("text/plain".toMediaType())
    }

    fun getLatRequestBody(): RequestBody {
        return lat.toString().toRequestBody("text/plain".toMediaType())
    }
}
