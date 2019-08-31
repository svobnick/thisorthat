package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Comment

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {

    private val commentsList = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_view, parent, false)
        return CommentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(commentsList[position])
    }

    class CommentsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var userId: TextView = view.findViewById(R.id.user_id)
        private var commentText: TextView = view.findViewById(R.id.comment_text)
//        private var commentId: TextView = view.findViewById(R.id.comment_id)
//        private var parentId: TextView = view.findViewById(R.id.parent_id)

        fun bind(comment: Comment) {
            userId.text = comment.text
//            commentId.text = comment.commentId.toString()
            commentText.text = comment.text
//            parentId.text = comment.parentId.toString()
        }
    }

    fun setComments(it: List<Comment>) {
        commentsList.addAll(it)
        notifyDataSetChanged()
    }
}