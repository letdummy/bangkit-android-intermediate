package com.sekalisubmit.storymu.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sekalisubmit.storymu.data.local.room.FetchPreference
import kotlinx.coroutines.launch

class ProfileViewModel(private val pref: FetchPreference): ViewModel() {
    fun getFetch(): LiveData<Boolean> {
        return pref.getFetch().asLiveData()
    }

    fun saveFetch(fetch: Boolean) {
        viewModelScope.launch {
            pref.saveFetch(fetch)
        }
    }
}