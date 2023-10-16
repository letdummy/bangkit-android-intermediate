package com.sekalisubmit.storymu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.data.remote.response.RegisterResponse
import com.sekalisubmit.storymu.data.remote.retrofit.ApiConfig
import com.sekalisubmit.storymu.data.repository.LoginRepository
import com.sekalisubmit.storymu.ui.fragment.RegisterFragment

class RegisterViewModel (application: Application): ViewModel() {
    companion object {
        const val TAG = "RegisterViewModel"
    }

    private val cLoginRepository: LoginRepository by lazy { LoginRepository(application) }

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

    fun saveToken(data: RegisterFragment.Data) {
        val login = Login(data.email, data.password, data.token)
        cLoginRepository.insert(login)
    }
}