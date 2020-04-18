package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.my_question_single_view.*

class MyQuestionsAdapter : RecyclerView.Adapter<MyQuestionsAdapter.QuestionListViewHolder>() {

    private val myQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_question_single_view, parent, false)
        return QuestionListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myQuestionsList.size
    }

    override fun onBindViewHolder(holder: QuestionListViewHolder, position: Int) {
        holder.bind(myQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return myQuestionsList[position].id
    }

    fun addQuestions(questions: List<Question>) {
        myQuestionsList.addAll(questions)
        notifyDataSetChanged()
    }

    class QuestionListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        LayoutContainer {
        override val containerView: View get() = itemView

        fun bind(question: Question) {
            val (firstPercent, lastPercent) = computeQuestionsPercentage(
                question.firstRate,
                question.lastRate
            )
            m_first_text.text = question.firstText
            setFirstStat(firstPercent, question.firstRate)
            m_last_text.text = question.lastText
            setLastStat(lastPercent, question.lastRate)
        }

        private fun setFirstStat(percentage: Int, amount: Int) {
            m_first_percent_value.text = percentage.toString()
            m_first_peoples_amount.text = amount.toString()
        }

        private fun setLastStat(percentage: Int, amount: Int) {
            m_last_percent_value.text = percentage.toString()
            m_last_peoples_amount.text = amount.toString()
        }
    }
}