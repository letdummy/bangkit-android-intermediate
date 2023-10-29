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
import com.sekalisubmit.storymu.ui.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
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
                    navController.navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
                    true
                }
                else -> false
            }
        }

        binding.addStory.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToCreateActivity())
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

}