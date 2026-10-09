package com.cinephile.data.mapper

import com.cinephile.data.remote.dto.TmdbCreditMovieDto
import com.cinephile.data.remote.dto.TmdbMovieDetailsDto
import com.cinephile.data.remote.dto.TmdbMovieDto
import com.cinephile.domain.Movie

fun TmdbMovieDto.toMovie(): Movie {
    return Movie(
        id = id,
        title = title ?: originalTitle ?: "Titre inconnu",
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        director = null,
        cast = emptyList(),
        genreIds = genreIds ?: emptyList(),
        genreNames = emptyList(),
        voteAverage = voteAverage ?: 0.0,
        popularity = popularity ?: 0.0
    )
}

fun TmdbMovieDetailsDto.toMovie(): Movie {
    val director = credits
        ?.crew
        ?.firstOrNull { crewMember ->
            crewMember.job.equals("Director", ignoreCase = true)
        }
        ?.name

    val castNames = credits
        ?.cast
        ?.sortedBy { it.order ?: Int.MAX_VALUE }
        ?.mapNotNull { it.name }
        ?.take(5)
        ?: emptyList()

    return Movie(
        id = id,
        title = title ?: originalTitle ?: "Titre inconnu",
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        director = director,
        cast = castNames,
        genreIds = genres.map { it.id },
        genreNames = genres.map { it.name },
        voteAverage = voteAverage ?: 0.0,
        popularity = popularity ?: 0.0
    )
}

fun TmdbCreditMovieDto.toMovieDto(): TmdbMovieDto {
    return TmdbMovieDto(
        adult = adult,
        backdropPath = backdropPath,
        genreIds = genreIds,
        id = id,
        originalLanguage = originalLanguage,
        originalTitle = originalTitle,
        overview = overview,
        popularity = popularity,
        posterPath = posterPath,
        releaseDate = releaseDate,
        title = title,
        voteAverage = voteAverage
    )
}