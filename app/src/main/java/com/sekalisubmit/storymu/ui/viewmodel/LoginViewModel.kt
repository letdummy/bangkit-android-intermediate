package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.data.repository.LoginRepository

class LoginViewModel(application: Application): ViewModel() {
    companion object {
        const val TAG = "LoginViewModel"
    }

    private val _login = MutableLiveData<LoginResponse>()
    val login get() = _login

    private val cLoginRepository: LoginRepository by lazy { LoginRepository(application) }

    suspend fun login(email: String, password: String): LoginResponse {
        val apiService = ApiConfig.getApiService()
        return apiService.login(email, password)
    }

    fun saveToken(email: String, password: String, token: String?) {
        val login = Login(email, password, token)
        cLoginRepository.insert(login)
    }
}