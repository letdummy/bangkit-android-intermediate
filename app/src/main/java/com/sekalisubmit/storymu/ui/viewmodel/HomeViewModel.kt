package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.local.room.story.Story
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.data.repository.LoginRepository
import com.sekalisubmit.storymu.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application, pref: UserPreference): ViewModel() {
    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> = _token

    private val storyRepository = StoryRepository(application)
    private val loginRepository = LoginRepository(application)

    private val _stories = MutableLiveData<List<Story?>>()
    val stories: LiveData<List<Story?>> = _stories

    init {
        loadTokenFromPref(pref)
    }

    fun getUser(): LiveData<Login> {
        return loginRepository.getUserData()
    }

    fun deleteUser() {
        viewModelScope.launch(Dispatchers.IO) {
            loginRepository.delete()
        }
    }

    private fun loadTokenFromPref(pref: UserPreference) {
        viewModelScope.launch(Dispatchers.IO) {
            val tokenFromPref = pref.getToken()
            withContext(Dispatchers.Main) {
                _token.value = tokenFromPref.first()
            }
        }
    }
    fun fetchStories(token: String) {
        val apiService = ApiConfig.getApiService(token)
        viewModelScope.launch(Dispatchers.IO) {
            val response = apiService.getStories()
            if (response.listStory != null) {
                val stories = response.listStory
                val listStory = stories.filterNotNull().map {
                    Story(
                        it.id!!,
                        it.photoUrl,
                        it.createdAt,
                        it.name,
                        it.description,
                        it.lon as Double?,
                        it.lat as Double?
                    )
                }
                // insert to database
                storyRepository.insert(listStory)
                _stories.postValue(listStory)
            } else {
                Log.e("HomeViewModel", "Error: ${response.message}")
            }
        }
    }

}