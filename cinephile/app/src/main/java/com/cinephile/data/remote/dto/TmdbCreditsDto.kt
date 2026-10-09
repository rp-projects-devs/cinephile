package com.cinephile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbCreditsDto(
    @SerializedName("cast")
    val cast: List<TmdbCastDto> = emptyList(),

    @SerializedName("crew")
    val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbCastDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("character")
    val character: String? = null,

    @SerializedName("order")
    val order: Int? = null
)

data class TmdbCrewDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("job")
    val job: String? = null,

    @SerializedName("department")
    val department: String? = null
)