package com.cinephile.data.bdd

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rating_films")
data class Film(
    @PrimaryKey val idFilm: Int,
    val note: Float = 0.0f,
    val estFavori: Boolean = false
)

@Entity(tableName = "watchlists")
data class Watchlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isMain: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist_movies",
    foreignKeys = [
        ForeignKey(
            entity = Watchlist::class,
            parentColumns = ["id"],
            childColumns = ["watchlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["watchlistId", "movieId"], unique = true)]
)
data class WatchlistMovie(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val watchlistId: Long,
    val movieId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val watchlistId: Long,
    val createdAt: Long = System.currentTimeMillis()
)