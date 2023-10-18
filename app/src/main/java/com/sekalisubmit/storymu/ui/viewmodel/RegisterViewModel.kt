package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.data.remote.response.RegisterResponse
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.data.repository.LoginRepository

class RegisterViewModel (application: Application, private val pref: UserPreference): ViewModel() {
    private val userRepository: LoginRepository by lazy { LoginRepository(application) }

    private val _register = MutableLiveData<RegisterResponse>()
    val register get() = _register

    suspend fun register(username: String, email: String, password: String): RegisterResponse {
        val apiService = ApiConfig.getApiService()
        return apiService.register(username, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val apiService = ApiConfig.getApiService()
        return apiService.login(email, password)
    }

    fun insertUserData(login: Login) {
        userRepository.insert(login)
    }

    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    suspend fun saveToken(token: String) {
        pref.saveToken(token)
    }
}