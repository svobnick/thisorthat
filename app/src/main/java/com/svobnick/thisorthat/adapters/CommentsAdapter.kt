package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Comment
import de.hdodenhof.circleimageview.CircleImageView

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
        private var userId: TextView = view.findViewById(R.id.user_id)
        private var commentText: TextView = view.findViewById(R.id.comment_text)
        private var avatar: CircleImageView = view.findViewById(R.id.avatar)
        private var commentId: TextView = view.findViewById(R.id.comment_id)
        private var parentId: TextView = view.findViewById(R.id.parent_id)

        fun bind(comment: Comment) {
            picasso.load(comment.avatarUrl).into(avatar)
            userId.text = comment.text
            commentText.text = comment.text
            commentId.text = comment.commentId.toString()
            parentId.text = comment.parentId.toString()
        }
    }

    fun setComments(it: List<Comment>) {
        commentsList.addAll(it)
        notifyDataSetChanged()
    }
}