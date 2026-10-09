package com.cinephile.data.repository

import com.cinephile.BuildConfig
import com.cinephile.data.mapper.toMovie
import com.cinephile.data.mapper.toMovieDto
import com.cinephile.data.remote.TmdbApi
import com.cinephile.data.remote.TmdbClient
import com.cinephile.data.remote.dto.TmdbGenreDto
import com.cinephile.data.remote.dto.TmdbMovieDto
import com.cinephile.domain.Movie
import com.cinephile.domain.SearchFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale

class MovieRepository(
    private val api: TmdbApi = TmdbClient.api
) {

    private var cachedGenres: List<TmdbGenreDto>? = null

    suspend fun searchMovies(filters: SearchFilters): List<Movie> = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            throw IllegalStateException("La clé TMDB est manquante. Ajoute TMDB_API_KEY dans local.properties.")
        }

        if (filters.isEmpty()) {
            throw IllegalArgumentException("Saisis au moins un critère de recherche.")
        }

        if (filters.hasInvalidYear()) {
            throw IllegalArgumentException("L’année doit contenir exactement 4 chiffres, par exemple 1999.")
        }

        val year = filters.normalizedYear()
        val genres = getGenres()
        val requestedGenreId = findGenreId(filters.genre, genres)

        val baseMovies = when {
            filters.title.isNotBlank() -> {
                api.searchMovies(
                    query = filters.title.trim(),
                    primaryReleaseYear = year
                ).results
            }

            filters.director.isNotBlank() -> {
                searchMoviesByDirector(filters.director)
            }

            filters.actor.isNotBlank() -> {
                searchMoviesByActor(filters.actor)
            }

            requestedGenreId != null || year != null -> {
                api.discoverMovies(
                    withGenres = requestedGenreId?.toString(),
                    primaryReleaseYear = year
                ).results
            }

            else -> emptyList()
        }

        val detailedMovies = loadDetailsForSearchResults(baseMovies)

        detailedMovies
            .filter { movie -> movieMatchesFilters(movie, filters, requestedGenreId) }
            .distinctBy { it.id }
            .sortedByDescending { it.popularity }
    }

    suspend fun getMovieDetails(movieId: Int): Movie = withContext(Dispatchers.IO) {
        api.getMovieDetails(movieId).toMovie()
    }

    private suspend fun getGenres(): List<TmdbGenreDto> {
        val cached = cachedGenres

        if (cached != null) {
            return cached
        }

        val genres = api.getMovieGenres().genres
        cachedGenres = genres
        return genres
    }

    private suspend fun searchMoviesByActor(actorName: String): List<TmdbMovieDto> {
        val person = api.searchPeople(actorName.trim())
            .results
            .firstOrNull()
            ?: return emptyList()

        return api.getPersonMovieCredits(person.id)
            .cast
            .map { it.toMovieDto() }
    }

    private suspend fun searchMoviesByDirector(directorName: String): List<TmdbMovieDto> {
        val person = api.searchPeople(directorName.trim())
            .results
            .firstOrNull()
            ?: return emptyList()

        return api.getPersonMovieCredits(person.id)
            .crew
            .filter { credit ->
                credit.job.equals("Director", ignoreCase = true)
            }
            .map { it.toMovieDto() }
    }

    private suspend fun loadDetailsForSearchResults(movies: List<TmdbMovieDto>): List<Movie> {
        val limitedMovies = movies
            .distinctBy { it.id }
            .take(24)

        return coroutineScope {
            limitedMovies
                .map { movieDto ->
                    async {
                        runCatching {
                            api.getMovieDetails(movieDto.id).toMovie()
                        }.getOrElse {
                            movieDto.toMovie()
                        }
                    }
                }
                .awaitAll()
        }
    }

    private fun movieMatchesFilters(
        movie: Movie,
        filters: SearchFilters,
        requestedGenreId: Int?
    ): Boolean {
        val titleMatches = filters.title.isBlank()
                || movie.title.contains(filters.title.trim(), ignoreCase = true)

        val yearMatches = filters.normalizedYear() == null
                || movie.releaseDate?.startsWith(filters.normalizedYear().orEmpty()) == true

        val directorMatches = filters.director.isBlank()
                || movie.director
            ?.contains(filters.director.trim(), ignoreCase = true) == true

        val actorMatches = filters.actor.isBlank()
                || movie.cast.any { actor ->
            actor.contains(filters.actor.trim(), ignoreCase = true)
        }

        val genreMatches = filters.genre.isBlank()
                || requestedGenreId == null
                || movie.genreIds.contains(requestedGenreId)
                || movie.genreNames.any { genreName ->
            genreName.normalizedForSearch()
                .contains(filters.genre.normalizedForSearch())
        }

        return titleMatches
                && yearMatches
                && directorMatches
                && actorMatches
                && genreMatches
    }

    private fun findGenreId(
        requestedGenre: String,
        genres: List<TmdbGenreDto>
    ): Int? {
        if (requestedGenre.isBlank()) {
            return null
        }

        val normalizedRequestedGenre = requestedGenre.normalizedForSearch()

        return genres.firstOrNull { genre ->
            val normalizedGenreName = genre.name.normalizedForSearch()

            normalizedGenreName == normalizedRequestedGenre
                    || normalizedGenreName.contains(normalizedRequestedGenre)
                    || normalizedRequestedGenre.contains(normalizedGenreName)
        }?.id
    }

    private fun String.normalizedForSearch(): String {
        return Normalizer
            .normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase(Locale.getDefault())
            .trim()
    }
}