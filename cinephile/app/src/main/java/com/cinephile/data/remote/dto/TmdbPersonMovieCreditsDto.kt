package com.cinephile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbPersonMovieCreditsDto(
    @SerializedName("cast")
    val cast: List<TmdbCreditMovieDto> = emptyList(),

    @SerializedName("crew")
    val crew: List<TmdbCreditMovieDto> = emptyList()
)

data class TmdbCreditMovieDto(
    @SerializedName("adult")
    val adult: Boolean? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("id")
    val id: Int,

    @SerializedName("original_language")
    val originalLanguage: String? = null,

    @SerializedName("original_title")
    val originalTitle: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("popularity")
    val popularity: Double? = null,

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double? = null,

    @SerializedName("job")
    val job: String? = null,

    @SerializedName("character")
    val character: String? = null
)