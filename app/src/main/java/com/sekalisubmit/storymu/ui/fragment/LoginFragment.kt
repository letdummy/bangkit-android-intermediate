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
import com.google.gson.annotations.SerializedName
import com.sekalisubmit.storymu.data.remote.response.LoginResponse
import com.sekalisubmit.storymu.databinding.FragmentLoginBinding
import com.sekalisubmit.storymu.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private val viewModel: LoginViewModel by lazy { LoginViewModel(requireActivity().application) }

    data class ErrorResponse(
        @field:SerializedName("error")
        val error: Boolean? = null,
        @field:SerializedName("message")
        val message: String? = null
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
            login()
        }
    }

    private fun navigateToRegisterFragment() {
        navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun validateInputFields() {
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
                    handleLoginResponse(response)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    handleLoginError(e)
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        val email = binding.edLoginEmail.text.toString()
        val password = binding.edLoginPassword.text.toString()
        if (response.error == false) {
            val token = response.loginResult?.token
            viewModel.saveToken(email, password, token)
            showNotification("success", "login")
        }
    }

    private fun handleLoginError(e: HttpException) {
        val jsonInString = e.response()?.errorBody()?.string()
        val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)

        when (errorBody?.message) {
            "\"email\" must be a valid email" -> showNotification("error", "invalidEmail")
            "Invalid password" -> showNotification("error", "invalidPassword")
        }
    }

    private fun showNotification(usage: String, type: String) {
        binding.notification.notificationSetter(usage, type)
        binding.notification.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
