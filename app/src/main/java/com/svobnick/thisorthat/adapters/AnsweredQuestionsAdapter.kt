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

class AnsweredQuestionsAdapter :
    RecyclerView.Adapter<AnsweredQuestionsAdapter.AnsweredQuestionsViewHolder>() {

    private val answeredQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnsweredQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return AnsweredQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return answeredQuestionsList.size
    }

    override fun onBindViewHolder(holder: AnsweredQuestionsViewHolder, position: Int) {
        holder.bind(answeredQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return answeredQuestionsList[position].id
    }

    fun setAnsweredQuestions(answeredQuestions: List<Question>) {
        answeredQuestionsList.addAll(answeredQuestions)
        notifyDataSetChanged()
    }

    class AnsweredQuestionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        LayoutContainer {
        override val containerView: View
            get() = itemView

        fun bind(question: Question) {
            val (firstPercent, lastPercent) = computeQuestionsPercentage(
                question.firstRate,
                question.lastRate
            )
            first_text.text = question.firstText
            first_percent.text = "$firstPercent%"
            last_text.text = question.lastText
            last_percent.text = "$lastPercent%"
        }
    }
}