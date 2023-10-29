package com.sekalisubmit.storymu.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.databinding.FragmentHomeBinding
import com.sekalisubmit.storymu.ui.adapter.StoryListAdapter
import com.sekalisubmit.storymu.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val pref = UserPreference.getInstance(requireContext().dataStore)
        viewModel = HomeViewModel(requireActivity().application, pref)

        setupRecyclerView()
        observeViewModel()

        return binding.root
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.layoutManager = layoutManager
        binding.rvHome.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))
        binding.rvHome.adapter = StoryListAdapter()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.token.observe(viewLifecycleOwner) { token ->
            token?.takeIf { it.isNotBlank() }?.let { nonEmptyToken ->
                viewModel.fetchStories(nonEmptyToken)
                viewModel.getUser().observe(viewLifecycleOwner) { user ->
                    binding.userHandler.text = "Welcome, ${user.name}"
                }
            }
        }

        viewModel.stories.observe(viewLifecycleOwner) { stories ->
//            viewLifecycleOwner.lifecycleScope.launch {
//                // finally, after adding delay, the rv bug is gone LOL
//                // I assume glide needs time to load the images
//                val adapter = binding.rvHome.adapter as StoryListAdapter
//                adapter.submitList(stories)
//                delay(2000)
//                binding.loadingHandlerHome.visibility = View.GONE
//                binding.rvHome.visibility = View.VISIBLE
//            }
            val adapter = binding.rvHome.adapter as StoryListAdapter
            adapter.submitList(stories)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLogout(pref: UserPreference) {
        viewModel.deleteUser()
        binding.btnLogout.visibility = View.GONE
        binding.userHandler.text = "Welcome, Guest"
        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                pref.deleteToken()

                binding.btnLogout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}