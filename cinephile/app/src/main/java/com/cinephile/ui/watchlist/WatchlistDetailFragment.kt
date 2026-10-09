package com.cinephile.ui.watchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.databinding.FragmentWatchlistDetailBinding
import com.cinephile.ui.search.MovieGridAdapter

class WatchlistDetailFragment : Fragment(R.layout.fragment_watchlist_detail) {

    private var _binding: FragmentWatchlistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistDetailViewModel by viewModels {
        WatchlistDetailViewModelFactory(
            AppDatabase.getDatabase(requireContext()).watchlistDao()
        )
    }

    private var currentWatchlistId: Long = -1

    private val movieAdapter = MovieGridAdapter(
        onMovieClick = { movie ->
            findNavController().navigate(
                R.id.movieDetailsFragment,
                Bundle().apply { putInt("movieId", movie.id) }
            )
        },
        onMovieLongClick = { movie ->
            AlertDialog.Builder(requireContext())
                .setTitle(movie.title)
                .setMessage("Retirer ce film de la watchlist ?")
                .setPositiveButton("Retirer") { _, _ ->
                    viewModel.removeMovie(currentWatchlistId, movie.id) { _, message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatchlistDetailBinding.bind(view)

        currentWatchlistId = arguments?.getLong("watchlistId") ?: return

        binding.watchlistDetailRecyclerView.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }

        viewModel.loadMovies(currentWatchlistId)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WatchlistDetailUiState.Loading -> {
                    binding.watchlistDetailProgressBar.isVisible = true
                    binding.watchlistDetailMessageTextView.isVisible = false
                }
                is WatchlistDetailUiState.Success -> {
                    binding.watchlistDetailProgressBar.isVisible = false
                    movieAdapter.submitList(state.movies)
                    binding.watchlistDetailMessageTextView.isVisible = state.movies.isEmpty()
                    binding.watchlistDetailMessageTextView.setText(R.string.search_empty_message)
                }
                is WatchlistDetailUiState.Error -> {
                    binding.watchlistDetailProgressBar.isVisible = false
                    binding.watchlistDetailMessageTextView.isVisible = true
                    binding.watchlistDetailMessageTextView.text = state.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}