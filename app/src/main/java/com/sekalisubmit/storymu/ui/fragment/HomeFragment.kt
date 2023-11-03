package com.sekalisubmit.storymu.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.UserPreference
import com.sekalisubmit.storymu.data.local.dataStore
import com.sekalisubmit.storymu.data.local.room.FetchPreference
import com.sekalisubmit.storymu.data.repository.LoginRepository
import com.sekalisubmit.storymu.databinding.FragmentHomeBinding
import com.sekalisubmit.storymu.ui.activity.MapsActivity
import com.sekalisubmit.storymu.ui.adapter.StoryListAdapter
import com.sekalisubmit.storymu.ui.viewmodel.HomeViewModel
import com.sekalisubmit.storymu.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class   HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var token: String

    private var alwaysOnline: Boolean = true

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

        lifecycleScope.launch {
            token = pref.getToken().first()
        }

        viewModel.stories.observe(viewLifecycleOwner) { stories ->
            Log.d("HomeFragment", "onCreateView: $stories")
            stories.forEach {
                Log.d("HomeFragment", "onCreateView: ${it?.name}")
            }
        }

        setupRecyclerView()
        observeUserData()
        loadingHandler()
        mapHandler()

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
                val loginRepository = LoginRepository(requireActivity().application)
                loginRepository.getUserData(it).observe(viewLifecycleOwner) { user ->
                    val welcome = getString(R.string.welcome)
                    binding.userHandler.text = "$welcome ${user.name}"
                }
            }
        }
    }

    private fun fetchStory() {
        if (isInternetConnected()) {
            viewModel.token.observe(viewLifecycleOwner) { token ->
                token?.takeIf { it.isNotBlank() }?.let { nonEmptyToken ->
                    binding.rvHome.visibility = View.GONE
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
            noInternetHelper()
        } else {
            noInternetHelper()
            loadStories()
        }
    }

    private fun noInternetHelper(){
        binding.apply {
            rvHome.visibility = View.GONE
            loadingHandler.visibility = View.GONE
            failHandler.visibility = View.VISIBLE
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

    private fun mapHandler() {
        binding.btnMap.setOnClickListener {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }
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

    override fun onResume() {
        super.onResume()
        fetchStory()
    }
}