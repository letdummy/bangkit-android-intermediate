package com.sekalisubmit.storymu.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.remote.PostStory
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CreateViewModel : ViewModel() {

    private val _submissionStatus = MutableLiveData<Boolean>()
    val submissionStatus: LiveData<Boolean> = _submissionStatus

    fun submitStory(pref: UserPreference, postStory: PostStory?){
        viewModelScope.launch {
            try {
                val token = pref.getToken().first()
                val apiService = ApiConfig.getApiService(token)
                val response = postStory?.let {
                    apiService.postStory(
                        it.getDescriptionRequestBody(),
                        postStory.photo,
                        postStory.getLonRequestBody(),
                        postStory.getLatRequestBody()
                    )
                }

                _submissionStatus.value = response?.error == false
            } catch (e: Exception) {
                _submissionStatus.value = false
            }
        }
    }

}