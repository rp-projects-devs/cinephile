package com.cinephile.data.bdd

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Insert
    suspend fun createWatchlist(watchlist: Watchlist): Long

    @Update
    suspend fun updateWatchlist(watchlist: Watchlist)

    @Delete
    suspend fun deleteWatchlist(watchlist: Watchlist)

    @Query("SELECT * FROM watchlists ORDER BY isMain DESC, createdAt ASC")
    fun getAllWatchlists(): Flow<List<Watchlist>>

    // Passer une watchlist en principale (une seule à la fois)
    @Transaction
    suspend fun setAsMain(watchlistId: Long) {
        clearMain()
        setMain(watchlistId)
    }

    @Query("UPDATE watchlists SET isMain = 0")
    suspend fun clearMain()

    @Query("UPDATE watchlists SET isMain = 1 WHERE id = :id")
    suspend fun setMain(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovie(entry: WatchlistMovie)

    @Query("DELETE FROM watchlist_movies WHERE watchlistId = :watchlistId AND movieId = :movieId")
    suspend fun removeMovie(watchlistId: Long, movieId: Int)

    @Query("SELECT * FROM watchlist_movies WHERE watchlistId = :watchlistId")
    fun getMoviesInWatchlist(watchlistId: Long): Flow<List<WatchlistMovie>>

    @Query("SELECT * FROM watchlists WHERE isMain = 1 LIMIT 1")
    suspend fun getMainWatchlist(): Watchlist?

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE watchlistId = :watchlistId AND movieId = :movieId")
    suspend fun isMovieInWatchlist(watchlistId: Long, movieId: Int): Int

    @Query("SELECT * FROM watchlists WHERE id = :id")
    suspend fun getWatchlistById(id: Long): Watchlist?

    @Query("SELECT movieId FROM watchlist_movies WHERE watchlistId = :watchlistId ORDER BY addedAt ASC LIMIT 1")
    suspend fun getFirstMovieId(watchlistId: Long): Int?
}