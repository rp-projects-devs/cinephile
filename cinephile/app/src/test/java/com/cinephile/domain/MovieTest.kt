package com.cinephile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieTest {

    private fun movie(
        posterPath: String? = null,
        releaseDate: String? = null,
        director: String? = null,
        cast: List<String> = emptyList()
    ) = Movie(
        id = 1,
        title = "Titanic",
        overview = null,
        posterPath = posterPath,
        releaseDate = releaseDate,
        director = director,
        cast = cast,
        genreIds = emptyList(),
        genreNames = emptyList(),
        voteAverage = 0.0,
        popularity = 0.0
    )

    @Test
    fun `un chemin de poster TMDB est complete avec l'URL des images`() {
        assertEquals(
            "https://image.tmdb.org/t/p/w500/abc.jpg",
            movie(posterPath = "/abc.jpg").posterUrl
        )
    }

    @Test
    fun `une URL de poster complete est conservee telle quelle`() {
        val url = "https://example.org/poster.jpg"
        assertEquals(url, movie(posterPath = url).posterUrl)
    }

    @Test
    fun `sans poster, l'URL est nulle`() {
        assertNull(movie(posterPath = null).posterUrl)
    }

    @Test
    fun `l'annee de sortie est extraite de la date`() {
        assertEquals("1997", movie(releaseDate = "1997-12-19").releaseYear)
    }

    @Test
    fun `une date absente ou trop courte donne une annee inconnue`() {
        assertEquals("Date inconnue", movie(releaseDate = null).releaseYear)
        assertEquals("Date inconnue", movie(releaseDate = "97").releaseYear)
    }

    @Test
    fun `les libelles affichent une valeur par defaut quand l'information manque`() {
        assertEquals("Réalisateur inconnu", movie(director = null).directorLabel)
        assertEquals("Casting inconnu", movie(cast = emptyList()).castLabel)
    }

    @Test
    fun `le casting est affiche separe par des virgules`() {
        val labels = movie(director = "James Cameron", cast = listOf("Leonardo DiCaprio", "Kate Winslet"))
        assertEquals("James Cameron", labels.directorLabel)
        assertEquals("Leonardo DiCaprio, Kate Winslet", labels.castLabel)
    }
}
