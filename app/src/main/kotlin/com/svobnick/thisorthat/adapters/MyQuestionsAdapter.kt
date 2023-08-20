package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.databinding.SingleMyChoiceViewBinding
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.OnItemClickListener

class MyQuestionsAdapter(private val clickListener: OnItemClickListener) : RecyclerView.Adapter<MyQuestionsAdapter.QuestionListViewHolder>() {

    private val mQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListViewHolder {
        val binding = SingleMyChoiceViewBinding.inflate(LayoutInflater.from(parent.context))
        return QuestionListViewHolder(binding.root, binding, clickListener)
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

    fun clear() {
        mQuestionsList.clear()
        notifyDataSetChanged()
    }

    fun addQuestions(questions: List<Question>) {
        mQuestionsList.addAll(questions)
        notifyDataSetChanged()
    }

    class QuestionListViewHolder(
        private val itemView: View,
        private val binding: SingleMyChoiceViewBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(question: Question) {
            val (firstPercent, lastPercent) = computeQuestionsPercentage(
                question.firstRate,
                question.lastRate
            )
            binding.mFirstText.text = question.firstText
            setFirstStat(firstPercent, question.firstRate)
            binding.mLastText.text = question.lastText
            setLastStat(lastPercent, question.lastRate)
        }

        private fun setFirstStat(percentage: Int, amount: Int) {
            binding.mFirstPercentValue.text = percentage.toString()
            binding.mFirstPeoplesAmount.text = amount.toString()
        }

        private fun setLastStat(percentage: Int, amount: Int) {
            binding.mLastPercentValue.text = percentage.toString()
            binding.mLastPeoplesAmount.text = amount.toString()
        }

        override fun onClick(v: View) {
            listener.onItemClick(bindingAdapterPosition, false)
        }
    }
}