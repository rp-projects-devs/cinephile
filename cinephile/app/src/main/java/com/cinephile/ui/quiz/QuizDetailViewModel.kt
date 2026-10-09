package com.cinephile.ui.quiz

import androidx.lifecycle.*
import com.cinephile.data.bdd.QuizDao
import com.cinephile.data.bdd.WatchlistDao
import kotlinx.coroutines.launch

class QuizDetailViewModel(
    private val quizDao: QuizDao,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    private val _quizName = MutableLiveData<String>()
    val quizName: LiveData<String> = _quizName

    private val _watchlistName = MutableLiveData<String>()
    val watchlistName: LiveData<String> = _watchlistName

    private val _watchlistId = MutableLiveData<Long>()
    val watchlistId: LiveData<Long> = _watchlistId

    fun load(quizId: Long) = viewModelScope.launch {
        val quiz = quizDao.getQuizById(quizId) ?: return@launch
        _quizName.value = quiz.name
        _watchlistId.value = quiz.watchlistId
        val watchlist = watchlistDao.getWatchlistById(quiz.watchlistId)
        _watchlistName.value = watchlist?.name ?: "Watchlist inconnue"
    }
}

class QuizDetailViewModelFactory(
    private val quizDao: QuizDao,
    private val watchlistDao: WatchlistDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizDetailViewModel::class.java))
            return QuizDetailViewModel(quizDao, watchlistDao) as T
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}