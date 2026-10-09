package com.cinephile.ui.details

import androidx.lifecycle.*
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.bdd.Film
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.bdd.WatchlistMovie
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val database: AppDatabase,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    private val _isInWatchlist = MutableLiveData<Boolean>(false)
    val isInWatchlist: LiveData<Boolean> = _isInWatchlist

    fun getFilm(movieId: Int): LiveData<Film?> =
        database.filmDao().recupererFilmParId(movieId).asLiveData()

    fun chargerEtatWatchlist(movieId: Int) {
        viewModelScope.launch {
            val mainWatchlist = watchlistDao.getMainWatchlist() ?: return@launch
            _isInWatchlist.value = watchlistDao.isMovieInWatchlist(mainWatchlist.id, movieId) > 0
        }
    }

    fun toggleWatchlist(movieId: Int, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val mainWatchlist = watchlistDao.getMainWatchlist()
            if (mainWatchlist == null) {
                onResult(false, "Aucune watchlist principale définie.")
                return@launch
            }
            val isIn = watchlistDao.isMovieInWatchlist(mainWatchlist.id, movieId) > 0
            try {
                if (isIn) {
                    watchlistDao.removeMovie(mainWatchlist.id, movieId)
                    _isInWatchlist.value = false
                    onResult(true, "Retiré de \"${mainWatchlist.name}\"")
                } else {
                    watchlistDao.addMovie(
                        WatchlistMovie(
                            watchlistId = mainWatchlist.id,
                            movieId = movieId
                        )
                    )
                    _isInWatchlist.value = true
                    onResult(true, "Ajouté à \"${mainWatchlist.name}\"")
                }
            } catch (e: Exception) {
                onResult(false, "Erreur : ${e.message}")
            }
        }
    }

    fun sauvegarderNote(movieId: Int, note: Float, estFavori: Boolean) {
        viewModelScope.launch {
            database.filmDao().insererOuMettreAJourFilm(
                Film(idFilm = movieId, note = note, estFavori = estFavori)
            )
        }
    }

    fun toggleFavori(movieId: Int, noteActuelle: Float, favoriActuel: Boolean) {
        viewModelScope.launch {
            database.filmDao().insererOuMettreAJourFilm(
                Film(idFilm = movieId, note = noteActuelle, estFavori = !favoriActuel)
            )
        }
    }

    class Factory(
        private val database: AppDatabase,
        private val watchlistDao: WatchlistDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MovieDetailsViewModel(database, watchlistDao) as T
    }
}