package com.cinephile.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.repository.MovieRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WatchlistDetailViewModel(
    private val watchlistDao: WatchlistDao,
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _state = MutableLiveData<WatchlistDetailUiState>()
    val state: LiveData<WatchlistDetailUiState> = _state

    fun loadMovies(watchlistId: Long) {
        viewModelScope.launch {
            _state.value = WatchlistDetailUiState.Loading
            try {
                val movieIds = watchlistDao.getMoviesInWatchlist(watchlistId).first()
                val movies = movieIds.map { entry ->
                    movieRepository.getMovieDetails(entry.movieId)
                }
                _state.value = WatchlistDetailUiState.Success(movies)
            } catch (e: Exception) {
                _state.value = WatchlistDetailUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
    fun removeMovie(watchlistId: Long, movieId: Int, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            try {
                watchlistDao.removeMovie(watchlistId, movieId)
                onResult(true, "Film retiré de la watchlist.")
                loadMovies(watchlistId) // recharge la liste
            } catch (e: Exception) {
                onResult(false, "Erreur lors de la suppression : ${e.message}")
            }
        }
    }

}

class WatchlistDetailViewModelFactory(
    private val watchlistDao: WatchlistDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistDetailViewModel::class.java)) {
            return WatchlistDetailViewModel(watchlistDao) as T
        }
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}