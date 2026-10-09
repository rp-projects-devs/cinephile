package com.cinephile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponseDto(
    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("results")
    val results: List<TmdbMovieDto> = emptyList(),

    @SerializedName("total_pages")
    val totalPages: Int = 0,

    @SerializedName("total_results")
    val totalResults: Int = 0
)