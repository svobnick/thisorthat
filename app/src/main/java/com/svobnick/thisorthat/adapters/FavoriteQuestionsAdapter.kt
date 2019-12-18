package com.svobnick.thisorthat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.fragments.ChoiceStatFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.single_question_view.*
import kotlin.reflect.KFunction1

class FavoriteQuestionsAdapter(
    private val context: Context,
    private val onClick: KFunction1<@ParameterName(name = "itemId") Long, Unit>
) : RecyclerView.Adapter<FavoriteQuestionsAdapter.FavoriteQuestionsViewHolder>() {

    private val favoriteQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return FavoriteQuestionsViewHolder(context, view, this::removeFavoriteQuestion)
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

    class FavoriteQuestionsViewHolder(context: Context,
                                      itemView: View,
                                      private val onClick: KFunction1<@ParameterName(name = "index") Int, Unit>
    ) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View get() = itemView
        private val fragmentManager = (context as AppCompatActivity).supportFragmentManager
        private val firstStat = fragmentManager.findFragmentById(R.id.first_stat) as ChoiceStatFragment
        private val lastStat = fragmentManager.findFragmentById(R.id.last_stat) as ChoiceStatFragment

        fun bind(question: Question) {
            val (firstPercent, lastPercent) = computeQuestionsPercentage(
                question.firstRate,
                question.lastRate
            )
            first_text.text = question.firstText
            firstStat.setStat(firstPercent, question.firstRate, true)
            last_text.text = question.lastText
            lastStat.setStat(lastPercent, question.lastRate, true)
//            remove_favorite_button.setOnClickListener {
//                onClick(layoutPosition)
//            }
        }
    }
}