package com.sekalisubmit.storymu.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.local.room.FetchPreference
import com.sekalisubmit.storymu.databinding.FragmentHomeBinding
import com.sekalisubmit.storymu.ui.adapter.StoryListAdapter
import com.sekalisubmit.storymu.ui.viewmodel.HomeViewModel
import com.sekalisubmit.storymu.ui.viewmodel.ProfileViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private var alwaysOnline: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val pref = UserPreference.getInstance(requireContext().dataStore)
        viewModel = HomeViewModel(requireActivity().application, pref)

        val fetchPref = FetchPreference.getInstance(requireContext().dataStore)
        val profileViewModel = ProfileViewModel(fetchPref)
        profileViewModel.getFetch().observe(requireActivity()) { fetch ->
            alwaysOnline = fetch
        }

        setupRecyclerView()
        observeUserData()
        fetchStory()
        loadingHandler()

        return binding.root
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.layoutManager = layoutManager
        binding.rvHome.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))
        val adapter = StoryListAdapter()
        binding.rvHome.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun observeUserData() {
        viewModel.token.observe(viewLifecycleOwner) { token ->
            token?.takeIf { it.isNotBlank() }?.let {
                viewModel.getUser().observe(viewLifecycleOwner) { user ->
                    binding.userHandler.text = "Welcome, ${user.name}"
                }
            }
        }
    }

    private fun fetchStory() {
        if (isInternetConnected()) {
            viewModel.token.observe(viewLifecycleOwner) { token ->
                token?.takeIf { it.isNotBlank() }?.let { nonEmptyToken ->
                    viewModel.fetchStories(nonEmptyToken)
                    onlineLoadStories()
                }
            }
        } else {
            handleNoInternetConnection()
        }
    }

    private fun handleNoInternetConnection() {
        if (alwaysOnline) {
            binding.rvHome.visibility = View.GONE
            binding.loadingHandler.visibility = View.GONE
            binding.failHandler.visibility = View.VISIBLE
        } else {
            binding.failHandler.visibility = View.GONE
            binding.loadingHandler.visibility = View.GONE
            binding.rvHome.visibility = View.VISIBLE
            loadStories()
        }
    }

    private fun loadStories() {
        if (alwaysOnline) {
            onlineLoadStories()
        } else {
            viewModel.getStoryDB().observe(viewLifecycleOwner) { stories ->
                val adapter = binding.rvHome.adapter as StoryListAdapter
                adapter.submitList(stories)
                binding.rvHome.visibility = if (stories.isNotEmpty()) View.VISIBLE else View.GONE
                binding.failHandler.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun onlineLoadStories(){
        viewModel.stories.observe(viewLifecycleOwner) { stories ->
            val adapter = binding.rvHome.adapter as StoryListAdapter
            adapter.submitList(stories)
            binding.rvHome.visibility = if (stories.isNotEmpty()) View.VISIBLE else View.GONE
            binding.failHandler.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
    }

    private fun loadingHandler() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingHandler.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

