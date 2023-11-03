package com.sekalisubmit.storymu.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.databinding.ActivityMainBinding
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
                // we show bottom nav only if user is logged in
                showBottomNav()
            } else {
                navigateToLandingFragment()
            }
        }

        bottomNavController()
    }

    private fun navigateToLandingFragment() {
        navController.navigate(R.id.action_homeFragment_to_landingFragment)
    }

    private fun showBottomNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    private fun bottomNavController(){
        binding.btnNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_feed -> {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination == R.id.homeFragment) {
                        // I implemented this to make sure that the feed fragment is refreshed when user click on the feed menu
                        // I don't know if this is the best way to do it
                        navController.navigate(R.id.action_homeFragment_self)
                    } else if (currentDestination == R.id.profileFragment) {
                        navController.navigate(R.id.action_profileFragment_to_homeFragment)
                    }
                    true
                }
                R.id.dummy -> {
                    false
                }
                R.id.menu_profile -> {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination == R.id.homeFragment) {
                        navController.navigate(R.id.action_homeFragment_to_profileFragment)
                    } else if (currentDestination == R.id.profileFragment) {
                        navController.navigate(R.id.action_profileFragment_self)
                    }
                    true
                }
                else -> false
            }
        }

        binding.addStory.setOnClickListener {
            // to avoid illegal state exception
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.homeFragment) {
                navController.navigate(R.id.action_homeFragment_to_createActivity)
            } else if (currentDestination == R.id.profileFragment) {
                navController.navigate(R.id.action_profileFragment_to_homeFragment)
                binding.btnNav.selectedItemId = R.id.menu_feed
                navController.navigate(R.id.action_homeFragment_to_createActivity)
            }
        }
    }
}