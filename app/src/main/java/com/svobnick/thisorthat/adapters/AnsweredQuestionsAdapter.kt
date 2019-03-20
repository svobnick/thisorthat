package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question

class AnsweredQuestionsAdapter: RecyclerView.Adapter<AnsweredQuestionsAdapter.AnsweredQuestionsViewHolder>() {

    private val answeredQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnsweredQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.answered_question_view, parent, false)
        return AnsweredQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return answeredQuestionsList.size
    }

    override fun onBindViewHolder(holder: AnsweredQuestionsViewHolder, position: Int) {
        holder.bind(answeredQuestionsList[position])
    }

    class AnsweredQuestionsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private var firstText: TextView = view.findViewById(R.id.first_answered_text)
        private var secondText: TextView = view.findViewById(R.id.second_answered_text)

        fun bind(question: Question) {
            firstText.text = question.firstText
            secondText.text = question.secondText
        }
    }

    fun setAnsweredQuestions(answeredQuestions: List<Question>) {
        answeredQuestionsList.addAll(answeredQuestions)
        notifyDataSetChanged()
    }
}