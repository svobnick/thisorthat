package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.single_question_view.*
import kotlin.reflect.KFunction1

class FavoriteQuestionsAdapter(private val onClick: KFunction1<@ParameterName(name = "itemId") Long, Unit>) :
    RecyclerView.Adapter<FavoriteQuestionsAdapter.FavoriteQuestionsViewHolder>() {

    private val favoriteQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return FavoriteQuestionsViewHolder(view, this::removeFavoriteQuestion)
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

    private fun removeFavoriteQuestion(index: Int) {
        val question = favoriteQuestionsList.removeAt(index)
        notifyItemRemoved(index)
        onClick(question.id)
    }

    class FavoriteQuestionsViewHolder(
        itemView: View,
        private val onClick: KFunction1<@ParameterName(name = "index") Int, Unit>
    ) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View
            get() = itemView

        fun bind(question: Question) {
            val (firstPercent, lastPercent) = computeQuestionsPercentage(
                question.firstRate,
                question.lastRate
            )
            first_text.text = question.firstText
//            first_percent.text = "$firstPercent%"
//            last_text.text = question.lastText
//            last_percent.text = "$lastPercent%"
//            remove_favorite_button.setOnClickListener {
//                onClick(layoutPosition)
//            }
        }
    }
}