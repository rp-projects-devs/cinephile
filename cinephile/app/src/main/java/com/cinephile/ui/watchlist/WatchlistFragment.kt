package com.cinephile.ui.watchlist

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.databinding.FragmentWatchlistBinding
import com.cinephile.data.bdd.Watchlist

class WatchlistFragment : Fragment(R.layout.fragment_watchlist) {

    private val viewModel: WatchlistViewModel by viewModels {
        WatchlistViewModelFactory(
            AppDatabase.getDatabase(requireContext()).watchlistDao()
        )
    }
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private val adapter = WatchlistAdapter(
        onClick = { watchlist ->
            findNavController().navigate(
                R.id.watchlistDetailFragment,
                Bundle().apply { putLong("watchlistId", watchlist.id) }
            )
        },
        onLongClick = { watchlist ->
            showWatchlistOptionsDialog(watchlist)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatchlistBinding.bind(view)

        binding.watchlistRecyclerView.adapter = adapter
        binding.watchlistRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Bouton créer
        binding.fabCreateWatchlist.setOnClickListener {
            showCreateDialog()
        }

        viewModel.watchlists.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }
    private val nom = "Watchlist"
    private fun showCreateDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Nom de la watchlist"
            setText(nom)
            selectAll()
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Nouvelle watchlist")
            .setView(input)
            .setPositiveButton("Créer") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) viewModel.createWatchlist(name)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showWatchlistOptionsDialog(watchlist: Watchlist) {
        val options = if (watchlist.isMain) {
            arrayOf("Renommer", "Supprimer")
        } else {
            arrayOf("Renommer", "Définir comme principale", "Supprimer")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(watchlist.name)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Renommer" -> showRenameDialog(watchlist)
                    "Définir comme principale" -> viewModel.setAsMain(watchlist.id)
                    "Supprimer" -> viewModel.deleteWatchlist(watchlist)
                }
            }
            .show()
    }

    private fun showRenameDialog(watchlist: Watchlist) {
        val input = EditText(requireContext()).apply {
            setText(watchlist.name)
            selectAll()
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Renommer")
            .setView(input)
            .setPositiveButton("Valider") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) viewModel.renameWatchlist(watchlist, newName)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}