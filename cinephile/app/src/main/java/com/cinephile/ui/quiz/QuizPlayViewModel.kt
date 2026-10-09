package com.cinephile.ui.quiz

import androidx.lifecycle.*
import com.cinephile.data.bdd.WatchlistDao
import com.cinephile.data.repository.MovieRepository
import com.cinephile.domain.QuizGenerator
import com.cinephile.domain.QuizQuestion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuizPlayViewModel(
    private val watchlistDao: WatchlistDao,
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val questions = mutableListOf<QuizQuestion>()
    private var currentIndex = 0
    private var score = 0

    private val _currentQuestion = MutableLiveData<QuizQuestion?>()
    val currentQuestion: LiveData<QuizQuestion?> = _currentQuestion

    private val _progress = MutableLiveData<String>()
    val progress: LiveData<String> = _progress

    private val _finished = MutableLiveData<Pair<Int, Int>>() // score / total
    val finished: LiveData<Pair<Int, Int>> = _finished

    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading

    fun loadQuiz(watchlistId: Long) = viewModelScope.launch {
        _loading.value = true
        val movieIds = watchlistDao.getMoviesInWatchlist(watchlistId).first()
        val movies = movieIds.map { entry ->
            async { runCatching { movieRepository.getMovieDetails(entry.movieId) }.getOrNull() }
        }.awaitAll().filterNotNull()
        questions.clear()
        questions.addAll(QuizGenerator.generate(movies))
        currentIndex = 0
        score = 0
        _loading.value = false
        showNext()
    }

    fun answer(userAnswer: Boolean) {
        val question = _currentQuestion.value ?: return
        if (userAnswer == question.correctAnswer) score++
        currentIndex++
        showNext()
    }

    private fun showNext() {
        if (currentIndex < questions.size) {
            _currentQuestion.value = questions[currentIndex]
            _progress.value = "Question ${currentIndex + 1} / ${questions.size}"
        } else {
            _finished.value = Pair(score, questions.size)
        }
    }
}

class QuizPlayViewModelFactory(private val watchlistDao: WatchlistDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizPlayViewModel::class.java))
            return QuizPlayViewModel(watchlistDao) as T
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}