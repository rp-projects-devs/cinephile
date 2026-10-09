package com.cinephile.data.remote

import com.cinephile.data.remote.dto.TmdbGenreResponseDto
import com.cinephile.data.remote.dto.TmdbMovieDetailsDto
import com.cinephile.data.remote.dto.TmdbPersonMovieCreditsDto
import com.cinephile.data.remote.dto.TmdbPersonSearchResponseDto
import com.cinephile.data.remote.dto.TmdbSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("primary_release_year") primaryReleaseYear: String? = null
    ): TmdbSearchResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "fr-FR",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): TmdbMovieDetailsDto

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1
    ): TmdbPersonSearchResponseDto

    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(
        @Path("person_id") personId: Int,
        @Query("language") language: String = "fr-FR"
    ): TmdbPersonMovieCreditsDto

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("language") language: String = "fr-FR"
    ): TmdbGenreResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("language") language: String = "fr-FR",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("primary_release_year") primaryReleaseYear: String? = null
    ): TmdbSearchResponseDto
}