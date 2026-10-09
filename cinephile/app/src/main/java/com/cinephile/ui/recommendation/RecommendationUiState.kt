package com.cinephile.ui.recommendation

import com.cinephile.domain.Movie

sealed class RecommendationUiState {
    data object Idle : RecommendationUiState()
    data object Loading : RecommendationUiState()
    data object Empty : RecommendationUiState()
    data class Success(val movies: List<Movie>) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}