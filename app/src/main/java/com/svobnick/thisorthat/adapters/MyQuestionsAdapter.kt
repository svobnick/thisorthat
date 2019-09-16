package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question

class MyQuestionsAdapter : RecyclerView.Adapter<MyQuestionsAdapter.MyQuestionsViewHolder>() {
    private val VIEW_QUESTIONS = 1
    private val VIEW_PROGRESS_BAR = 0

    private val myQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return MyQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myQuestionsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (myQuestionsList[position] != null) VIEW_QUESTIONS else VIEW_PROGRESS_BAR
    }

    override fun onBindViewHolder(holder: MyQuestionsViewHolder, position: Int) {
        holder.bind(myQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return myQuestionsList[position].id
    }

    class MyQuestionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var firstText: TextView = view.findViewById(R.id.first_text)
        private var secondText: TextView = view.findViewById(R.id.last_text)

        fun bind(question: Question) {
            firstText.text = question.firstText
            secondText.text = question.secondText
        }
    }

    fun setMyQuestions(answeredQuestions: List<Question>) {
        myQuestionsList.addAll(answeredQuestions)
        notifyDataSetChanged()
    }
}