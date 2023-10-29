package com.sekalisubmit.storymu.ui.activity

import android.os.Bundle
import android.view.View
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
                // we show bottom nav only if user is logged in
                showBottomNav()
            } else {
                navigateToLandingFragment()
            }
        }

        bottomNavController()
    }

    private fun navigateToLandingFragment() {
        navController.navigate(HomeFragmentDirections.actionHomeFragmentToLandingFragment())
    }

    private fun showBottomNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    private fun bottomNavController(){
//        val btn: BottomNavigationView = findViewById(R.id.btn_nav)
//        btn.menu.getItem(1).isEnabled = false
        binding.btnNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_feed -> {
                    navController.navigate(HomeFragmentDirections.actionHomeFragmentSelf())
                    true
                }
                R.id.dummy -> {
                    false
                }
                R.id.menu_profile -> {

                    true
                }
                else -> false
            }
        }
    }

}