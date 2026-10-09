package com.cinephile.ui.watchlist

import com.cinephile.domain.Movie

sealed class WatchlistDetailUiState {
    data object Loading : WatchlistDetailUiState()
    data class Success(val movies: List<Movie>) : WatchlistDetailUiState()
    data class Error(val message: String) : WatchlistDetailUiState()
}