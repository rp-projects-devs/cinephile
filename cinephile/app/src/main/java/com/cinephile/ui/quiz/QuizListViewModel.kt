package com.cinephile.ui.quiz

import androidx.lifecycle.*
import com.cinephile.data.bdd.Quiz
import com.cinephile.data.bdd.QuizDao
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.remote.TmdbClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

data class QuizWithPoster(val quiz: Quiz, val posterUrl: String?)

class QuizListViewModel(
    private val quizDao: QuizDao,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    val quizzes: LiveData<List<QuizWithPoster>> = quizDao.getAllQuizzes()
        .map { list ->
            list.map { quiz ->
                val movieId = watchlistDao.getFirstMovieId(quiz.watchlistId)
                val posterPath = movieId?.let {
                    runCatching { TmdbClient.api.getMovieDetails(it).posterPath } // ← adapter au nom exact du DTO
                        .getOrNull()
                }
                val posterUrl = posterPath?.let { path ->
                    "https://image.tmdb.org/t/p/w185$path"
                }
                QuizWithPoster(quiz, posterUrl)
            }
        }
        .asLiveData()

    fun createQuiz(name: String, watchlistId: Long) = viewModelScope.launch {
        quizDao.createQuiz(Quiz(name = name, watchlistId = watchlistId))
    }

    fun deleteQuiz(quiz: Quiz) = viewModelScope.launch {
        quizDao.deleteQuiz(quiz)
    }
}

class QuizListViewModelFactory(
    private val quizDao: QuizDao,
    private val watchlistDao: WatchlistDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizListViewModel::class.java))
            return QuizListViewModel(quizDao, watchlistDao) as T
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}