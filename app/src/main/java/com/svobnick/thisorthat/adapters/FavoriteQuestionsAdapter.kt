package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.single_question_view.*

class FavoriteQuestionsAdapter : RecyclerView.Adapter<FavoriteQuestionsAdapter.FavoriteQuestionsViewHolder>() {
    private val favoriteQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_question_view, parent, false)
        return FavoriteQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favoriteQuestionsList.size
    }

    override fun onBindViewHolder(holder: FavoriteQuestionsViewHolder, position: Int) {
        holder.bind(favoriteQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return favoriteQuestionsList[position].id
    }

    fun setFavoriteQuestions(favoriteQuestions: List<Question>) {
        favoriteQuestionsList.addAll(favoriteQuestions)
        notifyDataSetChanged()
    }

    class FavoriteQuestionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View
            get() = itemView

        fun bind(question: Question) {
            first_text.text = question.firstText
            last_text.text = question.secondText
            hidden_id.text = question.id.toString()
        }
    }
}