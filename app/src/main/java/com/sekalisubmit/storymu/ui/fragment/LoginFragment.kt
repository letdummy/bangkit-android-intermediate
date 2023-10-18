package com.sekalisubmit.storymu.ui.fragment

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
import com.sekalisubmit.storymu.databinding.FragmentLoginBinding
import com.sekalisubmit.storymu.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val pref = UserPreference.getInstance(requireContext().dataStore)
        viewModel = LoginViewModel(requireActivity().application, pref)
        setupListeners()
        validateInputFields()

        return binding.root
    }

    private fun setupListeners() {
        binding.tvSignUp.setOnClickListener {
            navigateToRegisterFragment()
        }

        binding.edLoginEmail.addTextChangedListener { validateInputFields() }
        binding.edLoginPassword.addTextChangedListener { validateInputFields() }

        binding.btnLogin.setOnClickListener {
            binding.loadingHandler.visibility = View.VISIBLE
            login()
        }
    }

    private fun validateInputFields() {
        // check if email and password is valid
        val email = binding.edLoginEmail.text.toString()
        val password = binding.edLoginPassword.text.toString()


        val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.isNotEmpty() && password.length >= 8

        binding.btnLogin.isEnabled = isEmailValid && isPasswordValid
    }

    private fun login() {
        val email = binding.edLoginEmail.text.toString()
        val password = binding.edLoginPassword.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = viewModel.login(email, password)
                withContext(Dispatchers.Main) {
                    binding.loadingHandler.visibility = View.GONE
                    handleLoginResponse(response)
                    // here I save user data to local database
                    // lol idk why I save the password too
                    val data = Login(email, response.loginResult?.name, response.loginResult?.token)
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
        showNotification("success", "login")
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

    private fun navigateToRegisterFragment() {
        navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun navigateToHomeFragment() {
        navController.navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
    }

    private fun showNotification(usage: String, type: String) {
        // full list of usage and type
        // check ui/customview/Notification.kt
        binding.notification.notificationSetter(usage, type)
        binding.notification.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
