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

class AnsweredQuestionsAdapter(private val context: Context) : RecyclerView.Adapter<AnsweredQuestionsAdapter.AnsweredQuestionsViewHolder>() {

    private val answeredQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnsweredQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_question_view, parent, false)
        return AnsweredQuestionsViewHolder(context, view)
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

    class AnsweredQuestionsViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
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
        }
    }
}