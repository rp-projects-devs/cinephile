package com.cinephile.ui.watchlist

import androidx.lifecycle.*
import com.cinephile.data.bdd.Watchlist
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.remote.TmdbClient
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class WatchlistWithPoster(val watchlist: Watchlist, val posterUrl: String?)

class WatchlistViewModel(private val dao: WatchlistDao) : ViewModel() {

    val watchlists: LiveData<List<WatchlistWithPoster>> = dao.getAllWatchlists()
        .map { list ->
            list.map { watchlist ->
                val movieId = dao.getFirstMovieId(watchlist.id)
                val posterPath = movieId?.let {
                    runCatching { TmdbClient.api.getMovieDetails(it).posterPath }
                        .getOrNull()
                }
                val posterUrl = posterPath?.let { path ->
                    "https://image.tmdb.org/t/p/w185$path"
                }
                WatchlistWithPoster(watchlist, posterUrl)
            }
        }
        .asLiveData()

    fun createWatchlist(name: String) = viewModelScope.launch {
        dao.createWatchlist(Watchlist(name = name))
    }

    fun renameWatchlist(watchlist: Watchlist, newName: String) = viewModelScope.launch {
        dao.updateWatchlist(watchlist.copy(name = newName))
    }

    fun deleteWatchlist(watchlist: Watchlist) = viewModelScope.launch {
        dao.deleteWatchlist(watchlist)
    }

    fun setAsMain(id: Long) = viewModelScope.launch {
        dao.setAsMain(id)
    }
}

class WatchlistViewModelFactory(private val dao: WatchlistDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistViewModel::class.java))
            return WatchlistViewModel(dao) as T
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}