package com.cinephile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbGenreResponseDto(
    @SerializedName("genres")
    val genres: List<TmdbGenreDto> = emptyList()
)

data class TmdbGenreDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)