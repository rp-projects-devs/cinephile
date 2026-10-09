package com.cinephile.ui.recommendation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.databinding.FragmentRecommendationsBinding
import com.cinephile.ui.search.MovieGridAdapter

class RecommendationsFragment : Fragment(R.layout.fragment_recommendations) {

    private var _binding: FragmentRecommendationsBinding? = null
    private val binding: FragmentRecommendationsBinding
        get() = _binding ?: error("Binding non disponible.")

    private val viewModel: RecommendationsViewModel by viewModels {
        RecommendationsViewModel.Factory(
            AppDatabase.getDatabase(requireContext())
        )
    }

    private val recommendationAdapter = MovieGridAdapter(
        onMovieClick = { movie ->
            findNavController().navigate(
                R.id.movieDetailsFragment,
                bundleOf("movieId" to movie.id)
            )
        },
        onMovieLongClick = { movie ->
            Toast.makeText(
                requireContext(),
                getString(R.string.recommendations_long_click_message, movie.title),
                Toast.LENGTH_SHORT
            ).show()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRecommendationsBinding.bind(view)

        setupRecyclerView()
        setupActions()
        observeViewModel()

        viewModel.loadRecommendations()
    }

    private fun setupRecyclerView() {
        binding.recommendationsRecyclerView.apply {
            adapter = recommendationAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }
    }

    private fun setupActions() {
        binding.recommendationsRefreshButton.setOnClickListener {
            viewModel.loadRecommendations(forceRefresh = true)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                RecommendationUiState.Idle -> {
                    binding.recommendationsProgressBar.isVisible = false
                    binding.recommendationsMessageTextView.isVisible = true
                    binding.recommendationsMessageTextView.setText(
                        R.string.recommendations_initial_message
                    )
                    recommendationAdapter.submitList(emptyList())
                }

                RecommendationUiState.Loading -> {
                    binding.recommendationsProgressBar.isVisible = true
                    binding.recommendationsMessageTextView.isVisible = true
                    binding.recommendationsMessageTextView.setText(
                        R.string.recommendations_loading_message
                    )
                    binding.recommendationsRefreshButton.isEnabled = false
                }

                RecommendationUiState.Empty -> {
                    binding.recommendationsProgressBar.isVisible = false
                    binding.recommendationsMessageTextView.isVisible = true
                    binding.recommendationsMessageTextView.setText(
                        R.string.recommendations_empty_message
                    )
                    binding.recommendationsRefreshButton.isEnabled = true
                    recommendationAdapter.submitList(emptyList())
                }

                is RecommendationUiState.Success -> {
                    binding.recommendationsProgressBar.isVisible = false
                    binding.recommendationsMessageTextView.isVisible = false
                    binding.recommendationsRefreshButton.isEnabled = true
                    recommendationAdapter.submitList(state.movies)
                }

                is RecommendationUiState.Error -> {
                    binding.recommendationsProgressBar.isVisible = false
                    binding.recommendationsMessageTextView.isVisible = true
                    binding.recommendationsMessageTextView.text = state.message
                    binding.recommendationsRefreshButton.isEnabled = true
                    recommendationAdapter.submitList(emptyList())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}