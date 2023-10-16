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
import com.sekalisubmit.storymu.data.remote.response.RegisterResponse
import com.sekalisubmit.storymu.databinding.FragmentRegisterBinding
import com.sekalisubmit.storymu.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { findNavController() }

    private val viewModel: RegisterViewModel by lazy { RegisterViewModel(requireActivity().application) }

    data class Data(val email: String, val password: String, val token: String?)

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
        _binding =  FragmentRegisterBinding.inflate(inflater, container, false)

        binding.tvLogin.setOnClickListener {
            navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

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
            register()
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
                val response = viewModel.register(username, email, password)
                withContext(Dispatchers.Main) {
                    handleRegisterResponse(response)
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
            showNotification("success", "register")
        }
    }

    private fun handleRegisterError(e: HttpException) {
        val jsonInString = e.response()?.errorBody()?.string()
        val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)

        when (errorBody?.message){
            "\"email\" must be a valid email" -> showNotification("error", "invalidEmail")
            "Email is already taken" -> showNotification("error", "emailTaken")
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