package com.cinephile.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cinephile.R
import com.cinephile.databinding.ItemMovieGridBinding
import com.cinephile.domain.Movie

class MovieGridAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onMovieLongClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieGridAdapter.MovieViewHolder>(MovieDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            val context = binding.root.context

            binding.movieTitleTextView.text = movie.title
            binding.movieDirectorTextView.text = movie.director
                ?: context.getString(R.string.movie_director_unknown)
            binding.movieReleaseDateTextView.text = movie.releaseDate
                ?: context.getString(R.string.movie_release_date_unknown)

            binding.moviePosterImageView.load(movie.posterUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_movie_placeholder_24)
                error(R.drawable.ic_movie_placeholder_24)
            }

            binding.root.setOnClickListener {
                onMovieClick(movie)
            }

            binding.root.setOnLongClickListener {
                onMovieLongClick(movie)
                true
            }
        }
    }

    private object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}