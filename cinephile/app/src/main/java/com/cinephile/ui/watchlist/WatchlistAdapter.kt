package com.cinephile.ui.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cinephile.R
import com.cinephile.databinding.ItemWatchlistBinding

class WatchlistAdapter(
    private val onClick: (com.cinephile.data.bdd.Watchlist) -> Unit,
    private val onLongClick: (com.cinephile.data.bdd.Watchlist) -> Unit
) : ListAdapter<WatchlistWithPoster, WatchlistAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemWatchlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WatchlistWithPoster) {
            binding.watchlistNameTextView.text = item.watchlist.name
            binding.mainIndicator.isVisible = item.watchlist.isMain

            binding.watchlistCoverImageView.load(item.posterUrl) {
                placeholder(R.drawable.ic_movie_placeholder_24)
                crossfade(true)
            }

            binding.root.setOnClickListener { onClick(item.watchlist) }
            binding.root.setOnLongClickListener { onLongClick(item.watchlist); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWatchlistBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<WatchlistWithPoster>() {
        override fun areItemsTheSame(a: WatchlistWithPoster, b: WatchlistWithPoster) = a.watchlist.id == b.watchlist.id
        override fun areContentsTheSame(a: WatchlistWithPoster, b: WatchlistWithPoster) = a == b
    }
}