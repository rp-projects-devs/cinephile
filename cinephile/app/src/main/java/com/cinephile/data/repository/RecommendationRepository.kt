package com.cinephile.data.repository

import com.cinephile.BuildConfig
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.bdd.Film
import com.cinephile.data.mapper.toMovie
import com.cinephile.data.remote.TmdbApi
import com.cinephile.data.remote.TmdbClient
import com.cinephile.data.remote.dto.TmdbMovieDetailsDto
import com.cinephile.domain.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.min

class RecommendationRepository(
    private val database: AppDatabase,
    private val api: TmdbApi = TmdbClient.api
) {

    companion object {
        private const val NOTE_MINIMALE_AIMEE = 3.5f
        private const val NOMBRE_GENRES_UTILISES = 3
        private const val NOMBRE_RECOMMANDATIONS = 20
        private const val NOMBRE_MAX_FILMS_PROFIL = 20
    }

    suspend fun getRecommendations(): List<Movie> = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            throw IllegalStateException(
                "La clé TMDB est manquante. Ajoute TMDB_API_KEY dans local.properties."
            )
        }

        val filmDao = database.filmDao()

        val tousLesFilmsConnus = filmDao.recupererTousLesFilmsEnBase()

        if (tousLesFilmsConnus.isEmpty()) {
            return@withContext emptyList()
        }

        val filmsAimes = filmDao.recupererFilmsPourRecommendations(NOTE_MINIMALE_AIMEE)

        if (filmsAimes.isEmpty()) {
            return@withContext emptyList()
        }

        val idsFilmsConnus = tousLesFilmsConnus
            .map { it.idFilm }
            .toSet()

        val filmsUtilesPourProfil = tousLesFilmsConnus
            .filter { film ->
                film.estFavori ||
                        film.note >= NOTE_MINIMALE_AIMEE ||
                        film.note in 0.5f..2.0f
            }
            .distinctBy { it.idFilm }
            .take(NOMBRE_MAX_FILMS_PROFIL)

        val detailsFilmsProfil = chargerDetailsFilmsProfil(filmsUtilesPourProfil)

        val scoresGenres = calculerScoresGenres(detailsFilmsProfil)

        val meilleursGenres = scoresGenres
            .filter { (_, score) -> score > 0.0 }
            .toList()
            .sortedByDescending { (_, score) -> score }
            .take(NOMBRE_GENRES_UTILISES)
            .map { (genreId, _) -> genreId }

        if (meilleursGenres.isEmpty()) {
            return@withContext emptyList()
        }

        val candidats = chargerFilmsCandidats(meilleursGenres)

        candidats
            .distinctBy { it.id }
            .filter { movie -> movie.id !in idsFilmsConnus }
            .sortedByDescending { movie ->
                calculerScoreRecommandation(movie, scoresGenres)
            }
            .take(NOMBRE_RECOMMANDATIONS)
    }

    private suspend fun chargerDetailsFilmsProfil(
        films: List<Film>
    ): List<Pair<Film, TmdbMovieDetailsDto>> = coroutineScope {
        films.map { film ->
            async {
                runCatching {
                    film to api.getMovieDetails(film.idFilm)
                }.getOrNull()
            }
        }
            .awaitAll()
            .filterNotNull()
    }

    private fun calculerScoresGenres(
        detailsFilmsProfil: List<Pair<Film, TmdbMovieDetailsDto>>
    ): Map<Int, Double> {
        val scoresGenres = mutableMapOf<Int, Double>()

        for ((filmLocal, detailsTmdb) in detailsFilmsProfil) {
            val poidsFilm = calculerPoidsFilm(filmLocal)

            for (genre in detailsTmdb.genres) {
                val ancienScore = scoresGenres[genre.id] ?: 0.0
                scoresGenres[genre.id] = ancienScore + poidsFilm
            }
        }

        return scoresGenres
    }

    private fun calculerPoidsFilm(film: Film): Double {
        var poids = 0.0

        if (film.estFavori) {
            poids += 3.0
        }

        poids += when {
            film.note >= 4.5f -> 3.0
            film.note >= 3.5f -> 2.0
            film.note in 0.5f..2.0f -> -2.0
            else -> 0.0
        }

        return poids
    }

    private suspend fun chargerFilmsCandidats(
        meilleursGenres: List<Int>
    ): List<Movie> = coroutineScope {
        meilleursGenres.map { genreId ->
            async {
                runCatching {
                    api.discoverMovies(
                        withGenres = genreId.toString(),
                        sortBy = "popularity.desc",
                        page = 1
                    ).results.map { movieDto ->
                        movieDto.toMovie()
                    }
                }.getOrElse {
                    emptyList()
                }
            }
        }
            .awaitAll()
            .flatten()
    }

    private fun calculerScoreRecommandation(
        movie: Movie,
        scoresGenres: Map<Int, Double>
    ): Double {
        val scoreGenres = movie.genreIds.sumOf { genreId ->
            scoresGenres[genreId] ?: 0.0
        }

        val scoreNoteTmdb = movie.voteAverage * 0.7

        val scorePopularite = min(movie.popularity, 500.0) * 0.01

        return scoreGenres + scoreNoteTmdb + scorePopularite
    }
}