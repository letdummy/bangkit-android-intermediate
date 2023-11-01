package com.sekalisubmit.storymu.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.local.room.login.LoginDao
import com.sekalisubmit.storymu.data.local.room.login.LoginRoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LoginRepository(application: Application) {

    private var loginDao: LoginDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = LoginRoomDatabase.getDatabase(application)
        loginDao = db.loginDao()
    }

    fun insert(login: Login) {
        executorService.execute {
            loginDao.insert(login)
        }
    }

    fun delete(login: Login) {
        executorService.execute {
            loginDao.delete(login)
        }
    }

    fun getUserData(token: String): LiveData<Login> = loginDao.getUserData(token)

}