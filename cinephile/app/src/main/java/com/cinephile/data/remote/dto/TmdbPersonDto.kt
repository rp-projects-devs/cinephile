package com.cinephile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbPersonSearchResponseDto(
    @SerializedName("results")
    val results: List<TmdbPersonDto> = emptyList()
)

data class TmdbPersonDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)