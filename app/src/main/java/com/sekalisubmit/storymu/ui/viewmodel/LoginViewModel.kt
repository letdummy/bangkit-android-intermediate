package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.data.repository.LoginRepository

class LoginViewModel(application: Application, private val pref: UserPreference): ViewModel() {
    private val loginRepository = LoginRepository(application)

    suspend fun login(email: String, password: String): LoginResponse {
        val apiService = ApiConfig.getApiService()
        return apiService.login(email, password)
    }

    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    suspend fun saveToken(token: String) {
        pref.saveToken(token)
    }

    // save user data to local database
    fun insertUserData(login: Login) {
        loginRepository.insert(login)
    }


}