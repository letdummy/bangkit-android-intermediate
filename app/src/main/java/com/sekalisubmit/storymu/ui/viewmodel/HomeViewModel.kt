package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sekalisubmit.storymu.data.remote.response.ListStoryItem

class HomeViewModel(application: Application): ViewModel() {
    private val _listStoryItem = MutableLiveData<List<ListStoryItem>>()
    val listStoryItem get() = _listStoryItem

}