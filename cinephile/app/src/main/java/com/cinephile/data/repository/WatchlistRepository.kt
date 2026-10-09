package com.cinephile.data.repository

import com.cinephile.data.bdd.Watchlist
import com.cinephile.data.bdd.WatchlistDao

class WatchlistRepository(private val dao: WatchlistDao) {

    fun getAllWatchlists() = dao.getAllWatchlists()

    suspend fun createWatchlist(name: String) = dao.createWatchlist(Watchlist(name = name))

    suspend fun renameWatchlist(watchlist: Watchlist, newName: String) =
        dao.updateWatchlist(watchlist.copy(name = newName))

    suspend fun deleteWatchlist(watchlist: Watchlist) = dao.deleteWatchlist(watchlist)

    suspend fun setAsMain(id: Long) = dao.setAsMain(id)
}