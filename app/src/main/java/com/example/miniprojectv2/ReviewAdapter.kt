package com.example.miniprojectv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewerName: TextView = itemView.findViewById(R.id.tv_reviewer_name)
        val comment: TextView = itemView.findViewById(R.id.tv_comment)
        val rating: TextView = itemView.findViewById(R.id.tv_review_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.reviewerName.text = review.reviewerName
        holder.comment.text = review.comment
        holder.rating.text = "⭐ ${review.rating}"
    }

    override fun getItemCount(): Int = reviews.size
}
