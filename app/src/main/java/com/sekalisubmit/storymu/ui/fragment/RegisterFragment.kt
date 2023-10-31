package com.sekalisubmit.storymu.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.local.room.login.Login
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.data.remote.response.RegisterResponse
import com.sekalisubmit.storymu.databinding.FragmentRegisterBinding
import com.sekalisubmit.storymu.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentRegisterBinding.inflate(inflater, container, false)

        binding.tvLogin.setOnClickListener {
            navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        val pref = UserPreference.getInstance(requireContext().dataStore)
        viewModel = RegisterViewModel(requireActivity().application, pref)

        setupListeners()
        validateInputFields()

        return binding.root
    }

    private fun setupListeners() {
        binding.tvLogin.setOnClickListener {
            navigateToLoginFragment()
        }

        binding.edRegisterName.addTextChangedListener { validateInputFields() }
        binding.edRegisterEmail.addTextChangedListener { validateInputFields() }
        binding.edRegisterPassword.addTextChangedListener { validateInputFields() }

        binding.btnSignUp.setOnClickListener {
            binding.loadingHandler.visibility = View.VISIBLE
            if (isInternetConnected()) {
                register()
            } else {
                showNotification("error", "noInternet")
                binding.loadingHandler.visibility = View.GONE
            }
        }
    }

    private fun navigateToLoginFragment() {
        navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
    }

    private fun validateInputFields() {
        val username = binding.edRegisterName.text.toString()
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()

        val isUsernameValid = username.isNotEmpty() && username.length >= 8
        val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.isNotEmpty() && password.length >= 8

        binding.btnSignUp.isEnabled = isUsernameValid && isEmailValid && isPasswordValid
    }

    private fun register(){
        val username = binding.edRegisterName.text.toString()
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()

        lifecycleScope.launch(Dispatchers.IO){
            try {
                val registerResponse = viewModel.register(username, email, password)
                withContext(Dispatchers.Main) {
                    handleRegisterResponse(registerResponse)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    handleRegisterError(e)
                }
            }
        }
    }

    private fun handleRegisterResponse(response: RegisterResponse) {
        val username = binding.edRegisterName.text.toString()
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()

        if (response.error == false) {
            login(username, email, password)
        }
    }

    private fun handleRegisterError(e: HttpException) {
        val jsonInString = e.response()?.errorBody()?.string()
        val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)

        when (errorBody?.message){
            "\"email\" must be a valid email" -> showNotification("error", "invalidEmail")
            "Email is already taken" -> showNotification("error", "emailTaken")
        }
    }

    private fun login(username: String, email: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val loginResponse = viewModel.login(email, password)
                withContext(Dispatchers.Main) {
                    binding.loadingHandler.visibility = View.GONE
                    handleLoginResponse(loginResponse)
                    // here I save user data to local database
                    // lol idk why I save the password too
                    val data = Login(email,username, loginResponse.loginResult?.token)
                    viewModel.insertUserData(data)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    binding.loadingHandler.visibility = View.GONE
                    handleLoginError(e)
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        // no need to check if response.error is true or false
        // because this is handle for success login
        val token = response.loginResult?.token
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveToken(token!!)
        }
        showNotification("success", "register")
        lifecycleScope.launch {
            // here I add delay to make sure token is saved and the notification is shown
            delay(5000)
            viewModel.getToken().observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    navigateToHomeFragment()
                }
            }
        }
    }

    private fun handleLoginError(e: HttpException) {
        val jsonInString = e.response()?.errorBody()?.string()
        val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)

        when (errorBody?.message) {
            "\"email\" must be a valid email" -> showNotification("error", "invalidEmail")
            "Invalid password" -> showNotification("error", "invalidPassword")
        }
    }

    private fun navigateToHomeFragment() {
        navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToHomeFragment())
    }


    private fun showNotification(usage: String, type: String) {
        binding.notification.notificationSetter(usage, type)
        binding.notification.visibility = View.VISIBLE
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}