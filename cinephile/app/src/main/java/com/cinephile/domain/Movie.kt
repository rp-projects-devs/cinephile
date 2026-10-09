package com.cinephile.domain

data class Movie(
    val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val director: String?,
    val cast: List<String>,
    val genreIds: List<Int>,
    val genreNames: List<String>,
    val voteAverage: Double,
    val popularity: Double
) {
    val posterUrl: String?
        get() = posterPath?.let { path ->
            if (path.startsWith("http")) {
                path
            } else {
                "https://image.tmdb.org/t/p/w500$path"
            }
        }

    val releaseYear: String
        get() = releaseDate
            ?.takeIf { it.length >= 4 }
            ?.take(4)
            ?: "Date inconnue"

    val directorLabel: String
        get() = director ?: "Réalisateur inconnu"

    val castLabel: String
        get() = if (cast.isEmpty()) {
            "Casting inconnu"
        } else {
            cast.joinToString(", ")
        }
}