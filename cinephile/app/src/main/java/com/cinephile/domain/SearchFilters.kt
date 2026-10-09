package com.cinephile.domain

data class SearchFilters(
    val title: String = "",
    val year: String = "",
    val director: String = "",
    val actor: String = "",
    val genre: String = ""
) {
    fun isEmpty(): Boolean {
        return title.isBlank()
                && year.isBlank()
                && director.isBlank()
                && actor.isBlank()
                && genre.isBlank()
    }

    fun normalizedYear(): String? {
        val trimmedYear = year.trim()

        if (trimmedYear.isBlank()) {
            return null
        }

        return trimmedYear.takeIf {
            it.matches(Regex("\\d{4}"))
        }
    }

    fun hasInvalidYear(): Boolean {
        return year.isNotBlank() && normalizedYear() == null
    }
}