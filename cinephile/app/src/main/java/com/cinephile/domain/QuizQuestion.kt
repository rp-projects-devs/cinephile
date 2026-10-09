package com.cinephile.domain

data class QuizQuestion(
    val text: String,
    val correctAnswer: Boolean,
    val movieTitle: String
)