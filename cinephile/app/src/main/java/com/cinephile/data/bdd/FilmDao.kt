package com.cinephile.data.bdd

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {

    // --- Gestion des films (Notes et Favoris) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insererOuMettreAJourFilm(film: Film)

    @Query("SELECT * FROM rating_films WHERE idFilm = :id")
    fun recupererFilmParId(id: Int): Flow<Film?>

    @Query("SELECT * FROM rating_films WHERE estFavori = 1")
    fun recupererFilmsFavoris(): Flow<List<Film>>

    @Query("SELECT * FROM rating_films")
    suspend fun recupererTousLesFilmsEnBase(): List<Film>

    @Query("""
        SELECT * FROM rating_films
        WHERE estFavori = 1 OR note >= :noteMinimale
    """)
    suspend fun recupererFilmsPourRecommendations(noteMinimale: Float): List<Film>
}