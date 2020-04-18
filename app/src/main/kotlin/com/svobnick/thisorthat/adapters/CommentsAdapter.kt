package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.utils.RoundedCornersTransform
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.single_comment_view.*

class CommentsAdapter(private val picasso: Picasso) : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    private val commentsList = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_comment_view, parent, false)
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

    fun setComments(it: List<Comment>) {
        commentsList.addAll(it)
        notifyDataSetChanged()
    }

    class CommentsViewHolder(itemView: View, private val picasso: Picasso) :
        RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View
            get() = itemView

        fun bind(comment: Comment) {
            picasso.load(comment.avatarUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .transform(RoundedCornersTransform(32.0f))
                .into(avatar)
            user_id.text = comment.text
            comment_text.text = comment.text
            comment_id.text = comment.commentId.toString()
            parent_id.text = comment.parentId.toString()
        }
    }
}