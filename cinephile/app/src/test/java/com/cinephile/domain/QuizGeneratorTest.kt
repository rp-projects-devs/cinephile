package com.cinephile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizGeneratorTest {

    private fun movie(
        id: Int,
        title: String,
        releaseDate: String?,
        director: String?,
        mainActor: String?,
        genre: String?
    ) = Movie(
        id = id,
        title = title,
        overview = null,
        posterPath = null,
        releaseDate = releaseDate,
        director = director,
        cast = listOfNotNull(mainActor),
        genreIds = emptyList(),
        genreNames = listOfNotNull(genre),
        voteAverage = 0.0,
        popularity = 0.0
    )

    private val titanic = movie(1, "Titanic", "1997-12-19", "James Cameron", "Leonardo DiCaprio", "Drame")
    private val inception = movie(2, "Inception", "2010-07-16", "Christopher Nolan", "Leonardo DiCaprio", "Science-Fiction")
    private val alien = movie(3, "Alien", "1979-05-25", "Ridley Scott", "Sigourney Weaver", "Horreur")
    private val movies = listOf(titanic, inception, alien)

    @Test
    fun `aucune question sans film`() {
        assertTrue(QuizGenerator.generate(emptyList()).isEmpty())
    }

    @Test
    fun `avec un seul film, les quatre questions sont vraies`() {
        val questions = QuizGenerator.generate(listOf(titanic))

        assertEquals(4, questions.size)
        assertTrue(questions.all { it.correctAnswer })
        assertTrue(questions.all { it.movieTitle == "Titanic" })
        val texts = questions.map { it.text }.toSet()
        assertEquals(
            setOf(
                "\"Titanic\" est sorti en 1997.",
                "James Cameron a réalisé \"Titanic\".",
                "Leonardo DiCaprio est l'acteur principal de \"Titanic\".",
                "\"Titanic\" est un film de genre Drame."
            ),
            texts
        )
    }

    @Test
    fun `les informations manquantes ne produisent pas de question`() {
        val incomplete = movie(4, "Inconnu", releaseDate = null, director = null, mainActor = null, genre = null)
        assertTrue(QuizGenerator.generate(listOf(incomplete)).isEmpty())
    }

    @Test
    fun `chaque question est coherente avec le film qu'elle concerne`() {
        val byTitle = movies.associateBy { it.title }

        // Le generateur est aleatoire : on verifie la propriete sur de nombreux tirages.
        repeat(300) {
            val questions = QuizGenerator.generate(movies)

            assertTrue(questions.size <= 4 * movies.size)

            questions.forEach { question ->
                val movie = byTitle.getValue(question.movieTitle)
                val text = question.text

                val claimedYear = Regex("est sorti en (\\d{4})").find(text)?.groupValues?.get(1)
                val claimedDirector = Regex("^(.+) a réalisé ").find(text)?.groupValues?.get(1)
                val claimedActor = Regex("^(.+) est l'acteur principal").find(text)?.groupValues?.get(1)
                val claimedGenre = Regex("film de genre (.+)\\.$").find(text)?.groupValues?.get(1)

                val (claimed, actual) = when {
                    claimedYear != null -> claimedYear to movie.releaseYear
                    claimedDirector != null -> claimedDirector to movie.director
                    claimedActor != null -> claimedActor to movie.cast.first()
                    claimedGenre != null -> claimedGenre to movie.genreNames.first()
                    else -> error("Question inattendue : $text")
                }

                if (question.correctAnswer) {
                    assertEquals(text, actual, claimed)
                } else {
                    assertNotEquals(text, actual, claimed)
                }
            }
        }
    }
}
