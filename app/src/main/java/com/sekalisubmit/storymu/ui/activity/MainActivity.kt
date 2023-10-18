package com.sekalisubmit.storymu.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.databinding.ActivityMainBinding
import com.sekalisubmit.storymu.ui.fragment.HomeFragmentDirections
import com.sekalisubmit.storymu.ui.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = UserPreference.getInstance(this.dataStore)
        val loginViewModel = LoginViewModel(application, pref)

        loginViewModel.getToken().observe(this) { token ->
            if (token.isNotEmpty()) {
                // do nothing coz HomeFragment is the start destination
            } else {
                navigateToLandingFragment()
            }
        }

    }

    private fun navigateToLandingFragment() {
        navController.navigate(HomeFragmentDirections.actionHomeFragmentToLandingFragment())
    }

}