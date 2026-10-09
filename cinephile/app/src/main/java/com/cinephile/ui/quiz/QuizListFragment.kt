package com.cinephile.ui.quiz

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.bdd.Watchlist
import com.cinephile.databinding.FragmentQuizListBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuizListFragment : Fragment(R.layout.fragment_quiz_list) {

    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizListViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        QuizListViewModelFactory(db.quizDao(), db.watchlistDao())  // ← ajouter watchlistDao
    }


    private val adapter = QuizAdapter(
        onClick = { quiz ->
            findNavController().navigate(
                R.id.quizDetailFragment,
                Bundle().apply { putLong("quizId", quiz.id) }
            )
        },
        onLongClick = { quiz ->
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer le quizz")
                .setMessage("Voulez-vous supprimer « ${quiz.name} » ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deleteQuiz(quiz)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizListBinding.bind(view)

        binding.quizRecyclerView.apply {
            adapter = this@QuizListFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        binding.fabCreateQuiz.setOnClickListener {
            showCreateDialog()
        }

        viewModel.quizzes.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.quizMessageTextView.isVisible = items.isEmpty()
        }
    }

    private fun showCreateDialog() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val watchlists = db.watchlistDao().getAllWatchlists().first()
            if (watchlists.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Aucune watchlist")
                    .setMessage("Créez d'abord une watchlist avant de créer un quizz.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            val nameInput = EditText(requireContext()).apply { hint = "Nom du quizz" }
            val watchlistNames = watchlists.map { it.name }.toTypedArray()
            var selectedWatchlist: Watchlist = watchlists[0]

            AlertDialog.Builder(requireContext())
                .setTitle("Nouveau quizz")
                .setView(nameInput)
                .setSingleChoiceItems(watchlistNames, 0) { _, which ->
                    selectedWatchlist = watchlists[which]
                }
                .setPositiveButton("Créer") { _, _ ->
                    val name = nameInput.text.toString().trim()
                    if (name.isNotEmpty()) {
                        viewModel.createQuiz(name, selectedWatchlist.id)
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}