package com.cinephile.ui.details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cinephile.R
import com.cinephile.data.bdd.AppDatabase
import com.cinephile.data.remote.TmdbClient
import com.cinephile.databinding.FragmentMovieDetailsBinding
import kotlinx.coroutines.launch

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    // État local pour éviter des sauvegardes en boucle lors du restore
    private var estFavori = false
    private var noteActuelle = 0f

    // Instanciation du ViewModel
    private val viewModel: MovieDetailsViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        MovieDetailsViewModel.Factory(db, db.watchlistDao())
    }
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)
        _binding = FragmentMovieDetailsBinding.bind(view)

        val movieId = arguments?.getInt("movieId") ?: -1
        if (movieId == -1) return

        loadMovieDetails(movieId)
        observerDonneesBdd(movieId)

        viewModel.chargerEtatWatchlist(movieId)

        viewModel.isInWatchlist.observe(viewLifecycleOwner) { isIn ->
            binding.boutonWatchlist.text = if (isIn) "Retirer de la watchlist" else "Ajouter à la watchlist"
        }

        binding.boutonWatchlist.setOnClickListener {
            viewModel.toggleWatchlist(movieId) { _, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // --- Sauvegarde de la note ---
        binding.barreNotation.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                noteActuelle = rating
                viewModel.sauvegarderNote(movieId, rating, estFavori)
                Toast.makeText(context, "Note de $rating étoile(s) enregistrée", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Toggle favori ---
        binding.boutonFavoris.setOnClickListener {
            viewModel.toggleFavori(movieId, noteActuelle, estFavori)
        }
    }

    // Observe la BDD et met à jour l'UI quand les données changent
    private fun observerDonneesBdd(movieId: Int) {
        viewModel.getFilm(movieId).observe(viewLifecycleOwner) { film ->
            film?.let {
                estFavori = it.estFavori
                noteActuelle = it.note

                // Restaurer la note sans déclencher le listener
                binding.barreNotation.setOnRatingBarChangeListener(null)
                binding.barreNotation.rating = it.note
                binding.barreNotation.setOnRatingBarChangeListener { _, rating, fromUser ->
                    if (fromUser) {
                        noteActuelle = rating
                        viewModel.sauvegarderNote(movieId, rating, estFavori)
                        Toast.makeText(context, "Note de $rating étoile(s) enregistrée", Toast.LENGTH_SHORT).show()
                    }
                }
                
                if (it.estFavori) {
                    binding.boutonFavoris.text = "Retirer des favoris"
                } else {
                    binding.boutonFavoris.text = "Ajouter aux favoris"
                }
            }
        }
    }

    private fun loadMovieDetails(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movie = TmdbClient.api.getMovieDetails(movieId)
                val imageUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"

                binding.textTitreFilm.text = movie.title

                binding.imageAfficheFilm.load(imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.progress_horizontal)
                    error(android.R.drawable.stat_notify_error)
                }

                binding.textDateSortie.text = "Sortie : ${movie.releaseDate}"

                // Le directeur est dans movie.credits.crew
                val directeur = movie.credits?.crew?.firstOrNull { it.job == "Director" }
                binding.textDirecteur.text = if (directeur != null) {
                    "Réalisateur : ${directeur.name}"
                } else {
                    "Réalisateur : inconnu"
                }
                binding.textSynopsis.text = movie.overview?.ifEmpty { "Aucun synopsis disponible" }

                // Cast — on prend les 5 premiers acteurs
                val acteurs = movie.credits?.cast
                    ?.take(5)
                    ?.joinToString(", ") { it.name.toString() }
                    ?: "Inconnu"
                binding.textCast.text = acteurs

            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}