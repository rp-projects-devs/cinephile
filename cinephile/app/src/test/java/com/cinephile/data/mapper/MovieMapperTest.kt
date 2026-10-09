package com.cinephile.data.mapper

import com.cinephile.data.remote.dto.TmdbCastDto
import com.cinephile.data.remote.dto.TmdbCreditMovieDto
import com.cinephile.data.remote.dto.TmdbCreditsDto
import com.cinephile.data.remote.dto.TmdbCrewDto
import com.cinephile.data.remote.dto.TmdbGenreDto
import com.cinephile.data.remote.dto.TmdbMovieDetailsDto
import com.cinephile.data.remote.dto.TmdbMovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `un resultat de recherche est converti avec des valeurs par defaut`() {
        val movie = TmdbMovieDto(id = 7, title = null, originalTitle = null, genreIds = null).toMovie()

        assertEquals(7, movie.id)
        assertEquals("Titre inconnu", movie.title)
        assertTrue(movie.genreIds.isEmpty())
        assertEquals(0.0, movie.voteAverage, 0.0)
        assertEquals(0.0, movie.popularity, 0.0)
        assertNull(movie.director)
        assertTrue(movie.cast.isEmpty())
    }

    @Test
    fun `le titre original remplace un titre traduit absent`() {
        val movie = TmdbMovieDto(id = 1, title = null, originalTitle = "Titanic").toMovie()
        assertEquals("Titanic", movie.title)
    }

    @Test
    fun `le realisateur est le premier membre de l'equipe dont le metier est Director`() {
        val details = TmdbMovieDetailsDto(
            id = 597,
            title = "Titanic",
            credits = TmdbCreditsDto(
                crew = listOf(
                    TmdbCrewDto(name = "Jon Landau", job = "Producer"),
                    TmdbCrewDto(name = "James Cameron", job = "director")
                )
            )
        )

        assertEquals("James Cameron", details.toMovie().director)
    }

    @Test
    fun `le casting est trie par ordre d'apparition et limite a cinq noms`() {
        val cast = listOf(
            TmdbCastDto(name = "Sans ordre", order = null),
            TmdbCastDto(name = "Troisieme", order = 2),
            TmdbCastDto(name = "Premier", order = 0),
            TmdbCastDto(name = null, order = 1),
            TmdbCastDto(name = "Quatrieme", order = 3),
            TmdbCastDto(name = "Cinquieme", order = 4),
            TmdbCastDto(name = "Sixieme", order = 5)
        )
        val details = TmdbMovieDetailsDto(id = 1, credits = TmdbCreditsDto(cast = cast))

        assertEquals(
            listOf("Premier", "Troisieme", "Quatrieme", "Cinquieme", "Sixieme"),
            details.toMovie().cast
        )
    }

    @Test
    fun `les genres des details sont conserves en identifiants et en noms`() {
        val details = TmdbMovieDetailsDto(
            id = 1,
            genres = listOf(TmdbGenreDto(18, "Drame"), TmdbGenreDto(10749, "Romance"))
        )
        val movie = details.toMovie()

        assertEquals(listOf(18, 10749), movie.genreIds)
        assertEquals(listOf("Drame", "Romance"), movie.genreNames)
    }

    @Test
    fun `une filmographie est convertie en resultat de recherche sans perte`() {
        val credit = TmdbCreditMovieDto(
            id = 27205,
            title = "Inception",
            originalTitle = "Inception",
            releaseDate = "2010-07-16",
            posterPath = "/poster.jpg",
            genreIds = listOf(28, 878),
            voteAverage = 8.4,
            popularity = 90.0,
            job = "Director"
        )
        val dto = credit.toMovieDto()

        assertEquals(27205, dto.id)
        assertEquals("Inception", dto.title)
        assertEquals("2010-07-16", dto.releaseDate)
        assertEquals("/poster.jpg", dto.posterPath)
        assertEquals(listOf(28, 878), dto.genreIds)
        assertEquals(8.4, dto.voteAverage!!, 0.0)
        assertEquals(90.0, dto.popularity!!, 0.0)
    }
}
