package com.sekalisubmit.storymu.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.repository.LoginRepository
import com.sekalisubmit.storymu.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val loginRepository = LoginRepository(requireActivity().application)
        val pref = UserPreference.getInstance(requireContext().dataStore)

        loginRepository.getUserData().observe(viewLifecycleOwner) { user ->
            binding.nameProfileText.text = user.name
            binding.emailProfileText.text = user.email
            val token = user.token
            val censoredToken = user.token?.substring(0, 5) + "..." + token?.substring(token.length - 5, token.length)
            binding.tokenProfileText.text = censoredToken
        }

        binding.logoutProfile.setOnClickListener {
            lifecycleScope.launch {
                pref.deleteToken()
            }
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
            val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
            bottomNav.visibility = View.GONE
        }

        return binding.root
    }

}