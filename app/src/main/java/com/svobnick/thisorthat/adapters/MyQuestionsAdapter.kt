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

class MyQuestionsAdapter : RecyclerView.Adapter<MyQuestionsAdapter.MyQuestionsViewHolder>() {

    private val myQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return MyQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myQuestionsList.size
    }

    override fun onBindViewHolder(holder: MyQuestionsViewHolder, position: Int) {
        holder.bind(myQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return myQuestionsList[position].id
    }

    fun setMyQuestions(answeredQuestions: List<Question>) {
        myQuestionsList.addAll(answeredQuestions)
        notifyDataSetChanged()
    }

    class MyQuestionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
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