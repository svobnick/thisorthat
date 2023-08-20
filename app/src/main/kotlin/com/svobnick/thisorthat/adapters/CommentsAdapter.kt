package com.svobnick.thisorthat.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.databinding.SingleCommentViewBinding
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.utils.RoundedCornersTransform

class CommentsAdapter(private val picasso: Picasso) :
    RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    private val commentsList = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val binding = SingleCommentViewBinding.inflate(LayoutInflater.from(parent.context))
        return CommentsViewHolder(binding.root, binding, picasso)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(commentsList[position], position)
    }

    override fun getItemId(position: Int): Long {
        return commentsList[position].commentId
    }

    fun setComments(it: List<Comment>) {
        commentsList.addAll(it)
        notifyDataSetChanged()
    }

    fun addComment(comment: Comment) {
        commentsList.add(comment)
        notifyDataSetChanged()
    }

    fun clear() {
        commentsList.clear()
        notifyDataSetChanged()
    }

    class CommentsViewHolder(
        private val itemView: View,
        private val binding: SingleCommentViewBinding,
        private val picasso: Picasso
    ) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(comment: Comment, position: Int) {
            picasso.load(comment.avatarUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .transform(RoundedCornersTransform(24.0f))
                .into(binding.avatar)
            binding.userId.text = comment.userId.toString()
            binding.commentAuthor.text = comment.name
            if (position % 2 == 0) {
                binding.commentAuthor.setTextColor(Color.parseColor("#FF6642"))
            } else {
                binding.commentAuthor.setTextColor(Color.parseColor("#B454B7"))
            }
            binding.commentText.text = comment.text
            binding.commentId.text = comment.commentId.toString()
            binding.parentId.text = comment.parentId.toString()
        }
    }
}