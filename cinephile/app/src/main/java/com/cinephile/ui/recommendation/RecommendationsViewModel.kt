package com.cinephile.ui.recommendation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.repository.RecommendationRepository
import kotlinx.coroutines.launch

class RecommendationsViewModel(
    private val repository: RecommendationRepository
) : ViewModel() {

    private val _state = MutableLiveData<RecommendationUiState>(RecommendationUiState.Idle)
    val state: LiveData<RecommendationUiState> = _state

    fun loadRecommendations(forceRefresh: Boolean = false) {
        val currentState = _state.value

        if (!forceRefresh && currentState is RecommendationUiState.Success) {
            return
        }

        viewModelScope.launch {
            _state.value = RecommendationUiState.Loading

            try {
                val recommendations = repository.getRecommendations()

                _state.value = if (recommendations.isEmpty()) {
                    RecommendationUiState.Empty
                } else {
                    RecommendationUiState.Success(recommendations)
                }
            } catch (exception: Exception) {
                _state.value = RecommendationUiState.Error(
                    exception.message ?: "Erreur pendant le chargement des recommandations."
                )
            }
        }
    }

    class Factory(
        private val database: AppDatabase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecommendationsViewModel::class.java)) {
                val repository = RecommendationRepository(database)
                return RecommendationsViewModel(repository) as T
            }

            throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        }
    }
}