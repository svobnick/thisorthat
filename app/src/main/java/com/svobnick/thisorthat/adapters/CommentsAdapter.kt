package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Comment
import kotlinx.android.synthetic.main.comment_view.view.*

class CommentsAdapter(private val picasso: Picasso) : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    private val commentsList = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_view, parent, false)
        return CommentsViewHolder(view, picasso)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(commentsList[position])
    }

    override fun getItemId(position: Int): Long {
        return commentsList[position].commentId
    }

    class CommentsViewHolder(view: View, private val picasso: Picasso) : RecyclerView.ViewHolder(view) {
        fun bind(comment: Comment) {
            itemView.user_id
            picasso.load(comment.avatarUrl).into(itemView.avatar)
            itemView.user_id.text = comment.text
            itemView.comment_text.text = comment.text
            itemView.comment_id.text = comment.commentId.toString()
            itemView.parent_id.text = comment.parentId.toString()
        }
    }

    fun setComments(it: List<Comment>) {
        commentsList.addAll(it)
        notifyDataSetChanged()
    }
}