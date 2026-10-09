package com.cinephile.ui.quiz

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.databinding.FragmentQuizPlayBinding

class QuizPlayFragment : Fragment(R.layout.fragment_quiz_play) {

    private var _binding: FragmentQuizPlayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizPlayViewModel by viewModels {
        QuizPlayViewModelFactory(AppDatabase.getDatabase(requireContext()).watchlistDao())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizPlayBinding.bind(view)

        val watchlistId = arguments?.getLong("watchlistId") ?: return
        viewModel.loadQuiz(watchlistId)

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingView.isVisible = isLoading
            binding.quizContent.isVisible = !isLoading
        }

        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            question?.let { binding.quizQuestionTextView.text = it.text }
        }

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.quizProgressTextView.text = it
        }

        viewModel.finished.observe(viewLifecycleOwner) { (score, total) ->
            AlertDialog.Builder(requireContext())
                .setTitle("Quizz terminé !")
                .setMessage("Vous avez obtenu $score / $total")
                .setPositiveButton("Retour") { _, _ -> findNavController().popBackStack() }
                .setCancelable(false)
                .show()
        }

        binding.trueButton.setOnClickListener { viewModel.answer(true) }
        binding.falseButton.setOnClickListener { viewModel.answer(false) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}