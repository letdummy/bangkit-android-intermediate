package com.sekalisubmit.storymu.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sekalisubmit.storymu.databinding.FragmentLandingBinding


class LandingFragment : Fragment() {
    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { findNavController() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener {
            navController.navigate(LandingFragmentDirections.actionLandingFragmentToLoginFragment())
        }

        binding.btnRegister.setOnClickListener {
            navController.navigate(LandingFragmentDirections.actionLandingFragmentToRegisterFragment())
        }

        return binding.root
    }

}