package com.sekalisubmit.storymu.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.repository.LoginRepository
import com.sekalisubmit.storymu.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val application by lazy { requireActivity().application }
    private val loginRepository by lazy { LoginRepository(application) }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val pref = UserPreference.getInstance(requireContext().dataStore)

        val userData = loginRepository.getUserData()
        // print name from userData
        userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userHandler.text = "Hello, ${user.name}"
                binding.btnLogout.visibility = View.VISIBLE
            }
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                pref.deleteToken()
                loginRepository.delete()
                binding.btnLogout.visibility = View.GONE
            }
        }

        return binding.root
    }
}