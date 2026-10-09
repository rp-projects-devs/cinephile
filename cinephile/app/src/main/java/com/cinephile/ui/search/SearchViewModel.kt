package com.cinephile.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.bdd.WatchlistMovie
import com.cinephile.data.repository.MovieRepository
import com.cinephile.domain.Movie
import com.cinephile.domain.SearchFilters
import kotlinx.coroutines.launch

class SearchViewModel(
    private val movieRepository: MovieRepository,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    private val _state = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val state: LiveData<SearchUiState> = _state

    fun searchMovies(filters: SearchFilters) {
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val movies = movieRepository.searchMovies(filters)
                _state.value = SearchUiState.Success(movies)
            } catch (exception: Exception) {
                _state.value = SearchUiState.Error(
                    exception.message ?: "Une erreur inconnue est survenue."
                )
            }
        }
    }

    fun addToMainWatchlist(movieId: Int, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val mainWatchlist = watchlistDao.getMainWatchlist()
            if (mainWatchlist == null) {
                onResult(false, "Aucune watchlist principale définie.")
                return@launch
            }
            val alreadyIn = watchlistDao.isMovieInWatchlist(mainWatchlist.id, movieId) > 0
            if (alreadyIn) {
                onResult(false, "Film déjà dans la watchlist.")
                return@launch
            }
            try {
                watchlistDao.addMovie(WatchlistMovie(watchlistId = mainWatchlist.id, movieId = movieId))
                onResult(true, "Ajouté à \"${mainWatchlist.name}\"")
            } catch (e: Exception) {
                onResult(false, "Erreur lors de l'ajout : ${e.message}")
            }
        }
    }
    fun removeFromMainWatchlist(movieId: Int, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val mainWatchlist = watchlistDao.getMainWatchlist()
            if (mainWatchlist == null) {
                onResult(false, "Aucune watchlist principale définie.")
                return@launch
            }
            val isIn = watchlistDao.isMovieInWatchlist(mainWatchlist.id, movieId) > 0
            if (!isIn) {
                onResult(false, "Film non présent dans la watchlist.")
                return@launch
            }
            try {
                watchlistDao.removeMovie(mainWatchlist.id, movieId)
                onResult(true, "Retiré de \"${mainWatchlist.name}\"")
            } catch (e: Exception) {
                onResult(false, "Erreur lors de la suppression : ${e.message}")
            }
        }
    }
}

class SearchViewModelFactory(
    private val movieRepository: MovieRepository,
    private val watchlistDao: WatchlistDao  // ← ajouté
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(movieRepository, watchlistDao) as T
        }
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val movies: List<Movie>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}