package com.cinephile.ui.search

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.repository.MovieRepository
import com.cinephile.databinding.FragmentSearchBinding
import com.cinephile.domain.SearchFilters

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = _binding ?: error("Binding non disponible.")

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            movieRepository = MovieRepository(),
            watchlistDao = AppDatabase.getDatabase(requireContext()).watchlistDao()
        )
    }

    private val movieAdapter = MovieGridAdapter(
        onMovieClick = { movie ->
            findNavController().navigate(
                R.id.movieDetailsFragment,
                bundleOf("movieId" to movie.id)
            )
        },
        onMovieLongClick = { movie ->
            viewModel.addToMainWatchlist(movie.id) { success, message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchBinding.bind(view)

        setupRecyclerView()
        setupActions()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.movieRecyclerView.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }
    }

    private fun setupActions() {
        binding.searchButton.setOnClickListener {
            launchSearch()
        }

        binding.titleEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                launchSearch()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                SearchUiState.Idle -> {
                    binding.searchProgressBar.isVisible = false
                    binding.searchMessageTextView.isVisible = true
                    binding.searchMessageTextView.setText(R.string.search_intro_message)
                    movieAdapter.submitList(emptyList())
                }

                SearchUiState.Loading -> {
                    binding.searchProgressBar.isVisible = true
                    binding.searchMessageTextView.isVisible = true
                    binding.searchMessageTextView.setText(R.string.search_loading_message)
                }

                is SearchUiState.Success -> {
                    binding.searchProgressBar.isVisible = false
                    movieAdapter.submitList(state.movies)

                    binding.searchMessageTextView.isVisible = state.movies.isEmpty()
                    binding.searchMessageTextView.setText(R.string.search_empty_message)
                }

                is SearchUiState.Error -> {
                    binding.searchProgressBar.isVisible = false
                    binding.searchMessageTextView.isVisible = true
                    binding.searchMessageTextView.text = state.message
                    movieAdapter.submitList(emptyList())
                }
            }
        }
    }

    private fun launchSearch() {
        val filters = SearchFilters(
            title = binding.titleEditText.text?.toString().orEmpty(),
            year = binding.yearEditText.text?.toString().orEmpty(),
            director = binding.directorEditText.text?.toString().orEmpty(),
            actor = binding.actorEditText.text?.toString().orEmpty(),
            genre = binding.genreEditText.text?.toString().orEmpty()
        )

        viewModel.searchMovies(filters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}