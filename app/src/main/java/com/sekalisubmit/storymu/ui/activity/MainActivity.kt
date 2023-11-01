package com.sekalisubmit.storymu.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.databinding.ActivityMainBinding
import com.sekalisubmit.storymu.ui.fragment.HomeFragmentDirections
import com.sekalisubmit.storymu.ui.fragment.ProfileFragmentDirections
import com.sekalisubmit.storymu.ui.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.permission_true), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.permission_false), Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


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

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
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
        binding.btnNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_feed -> {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination == R.id.homeFragment) {
                        // I implemented this to make sure that the feed fragment is refreshed when user click on the feed menu
                        // I don't know if this is the best way to do it
                        navController.navigate(HomeFragmentDirections.actionHomeFragmentSelf())
                    } else if (currentDestination == R.id.profileFragment) {
                        navController.navigate(ProfileFragmentDirections.actionProfileFragmentToHomeFragment())
                    }
                    true
                }
                R.id.dummy -> {
                    false
                }
                R.id.menu_profile -> {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination == R.id.homeFragment) {
                        navController.navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
                    } else if (currentDestination == R.id.profileFragment) {
                        navController.navigate(ProfileFragmentDirections.actionProfileFragmentSelf())
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
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToCreateActivity())
            } else if (currentDestination == R.id.profileFragment) {
                navController.navigate(ProfileFragmentDirections.actionProfileFragmentToHomeFragment())
                binding.btnNav.selectedItemId = R.id.menu_feed
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToCreateActivity())
            }
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}