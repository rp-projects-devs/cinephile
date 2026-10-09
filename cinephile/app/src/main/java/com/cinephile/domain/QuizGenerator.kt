package com.cinephile.domain

object QuizGenerator {

    fun generate(movies: List<Movie>): List<QuizQuestion> {
        if (movies.isEmpty()) return emptyList()
        val questions = mutableListOf<QuizQuestion>()

        movies.forEach { movie ->
            val others = movies.filter { it.id != movie.id }

            // Date de sortie
            movie.releaseYear.takeIf { it != "Date inconnue" }?.let { year ->
                if (others.isNotEmpty() && Math.random() > 0.5) {
                    val wrongYear = others.random().releaseYear
                    if (wrongYear != year && wrongYear != "Date inconnue") {
                        questions.add(QuizQuestion(
                            text = "\"${movie.title}\" est sorti en $wrongYear.",
                            correctAnswer = false,
                            movieTitle = movie.title
                        ))
                    }
                } else {
                    questions.add(QuizQuestion(
                        text = "\"${movie.title}\" est sorti en $year.",
                        correctAnswer = true,
                        movieTitle = movie.title
                    ))
                }
            }

            // Réalisateur
            movie.director?.let { director ->
                if (others.isNotEmpty() && Math.random() > 0.5) {
                    val wrongDirector = others.mapNotNull { it.director }.randomOrNull()
                    if (wrongDirector != null && wrongDirector != director) {
                        questions.add(QuizQuestion(
                            text = "$wrongDirector a réalisé \"${movie.title}\".",
                            correctAnswer = false,
                            movieTitle = movie.title
                        ))
                    }
                } else {
                    questions.add(QuizQuestion(
                        text = "$director a réalisé \"${movie.title}\".",
                        correctAnswer = true,
                        movieTitle = movie.title
                    ))
                }
            }

            // Acteur principal
            movie.cast.firstOrNull()?.let { actor ->
                if (others.isNotEmpty() && Math.random() > 0.5) {
                    val wrongActor = others.mapNotNull { it.cast.firstOrNull() }.randomOrNull()
                    if (wrongActor != null && wrongActor != actor) {
                        questions.add(QuizQuestion(
                            text = "$wrongActor est l'acteur principal de \"${movie.title}\".",
                            correctAnswer = false,
                            movieTitle = movie.title
                        ))
                    }
                } else {
                    questions.add(QuizQuestion(
                        text = "$actor est l'acteur principal de \"${movie.title}\".",
                        correctAnswer = true,
                        movieTitle = movie.title
                    ))
                }
            }

            // Genre
            movie.genreNames.firstOrNull()?.let { genre ->
                if (others.isNotEmpty() && Math.random() > 0.5) {
                    val wrongGenre = others.flatMap { it.genreNames }.randomOrNull()
                    if (wrongGenre != null && wrongGenre != genre) {
                        questions.add(QuizQuestion(
                            text = "\"${movie.title}\" est un film de genre $wrongGenre.",
                            correctAnswer = false,
                            movieTitle = movie.title
                        ))
                    }
                } else {
                    questions.add(QuizQuestion(
                        text = "\"${movie.title}\" est un film de genre $genre.",
                        correctAnswer = true,
                        movieTitle = movie.title
                    ))
                }
            }
        }

        return questions.shuffled()
    }
}