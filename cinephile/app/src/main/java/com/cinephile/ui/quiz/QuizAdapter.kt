package com.cinephile.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cinephile.R
import com.cinephile.data.bdd.Quiz
import com.cinephile.databinding.ItemQuizBinding

class QuizAdapter(
    private val onClick: (Quiz) -> Unit,
    private val onLongClick: (Quiz) -> Unit
) : ListAdapter<QuizWithPoster, QuizAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemQuizBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizWithPoster) {
            binding.quizNameTextView.text = item.quiz.name

            binding.quizCoverImageView.load(item.posterUrl) {
                placeholder(R.drawable.ic_movie_placeholder_24)
                crossfade(true)
            }

            binding.root.setOnClickListener { onClick(item.quiz) }
            binding.root.setOnLongClickListener { onLongClick(item.quiz); true }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<QuizWithPoster>() {
        override fun areItemsTheSame(a: QuizWithPoster, b: QuizWithPoster) = a.quiz.id == b.quiz.id
        override fun areContentsTheSame(a: QuizWithPoster, b: QuizWithPoster) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

}