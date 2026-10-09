package com.cinephile.ui.quiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.databinding.FragmentQuizDetailBinding

class QuizDetailFragment : Fragment(R.layout.fragment_quiz_detail) {

    private var _binding: FragmentQuizDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizDetailViewModel by viewModels {
        QuizDetailViewModelFactory(
            AppDatabase.getDatabase(requireContext()).quizDao(),
            AppDatabase.getDatabase(requireContext()).watchlistDao()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizDetailBinding.bind(view)

        val quizId = arguments?.getLong("quizId") ?: return
        viewModel.load(quizId)

        viewModel.quizName.observe(viewLifecycleOwner) {
            binding.quizDetailNameTextView.text = it
        }

        viewModel.watchlistName.observe(viewLifecycleOwner) {
            binding.quizDetailWatchlistTextView.text = "Basé sur : $it"
        }

        viewModel.watchlistId.observe(viewLifecycleOwner) { watchlistId ->
            binding.launchQuizButton.setOnClickListener {
                findNavController().navigate(
                    R.id.quizPlayFragment,
                    Bundle().apply { putLong("watchlistId", watchlistId) }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}