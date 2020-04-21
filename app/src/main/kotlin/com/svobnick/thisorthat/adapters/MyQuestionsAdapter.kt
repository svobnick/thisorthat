package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.OnItemClickListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.single_my_choice_view.*

class MyQuestionsAdapter(private val clickListener: OnItemClickListener) : RecyclerView.Adapter<MyQuestionsAdapter.QuestionListViewHolder>() {

    private val mQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_my_choice_view, parent, false)
        return QuestionListViewHolder(view, clickListener)
    }

    override fun getItemCount(): Int {
        return mQuestionsList.size
    }

    override fun onBindViewHolder(holder: QuestionListViewHolder, position: Int) {
        holder.bind(mQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return mQuestionsList[position].id
    }

    fun getItem(position: Int): Question {
        return mQuestionsList[position]
    }

    fun addQuestions(questions: List<Question>) {
        mQuestionsList.addAll(questions)
        notifyDataSetChanged()
    }

    class QuestionListViewHolder(itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView), LayoutContainer, View.OnClickListener {
        override val containerView: View get() = itemView

        init {
            itemView.setOnClickListener(this)
        }

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

        override fun onClick(v: View) {
            listener.onItemClick(adapterPosition, false)
        }
    }
}