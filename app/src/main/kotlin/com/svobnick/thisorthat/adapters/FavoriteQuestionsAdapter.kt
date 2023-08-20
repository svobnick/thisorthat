package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.databinding.SingleFavoriteChoiceViewBinding
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.OnItemClickListener

class FavoriteQuestionsAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<FavoriteQuestionsAdapter.QuestionListViewHolder>() {

    private val fQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListViewHolder {
        val binding = SingleFavoriteChoiceViewBinding.inflate(LayoutInflater.from(parent.context))
        return QuestionListViewHolder(binding.root,binding, clickListener)
    }

    override fun getItemCount(): Int {
        return fQuestionsList.size
    }

    override fun onBindViewHolder(holder: QuestionListViewHolder, position: Int) {
        holder.bind(fQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return fQuestionsList[position].id
    }

    fun addQuestions(questions: List<Question>) {
        fQuestionsList.addAll(questions)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Question {
        return fQuestionsList[position]
    }

    fun clear() {
        fQuestionsList.clear()
        notifyDataSetChanged()
    }

    class QuestionListViewHolder(
        private val itemView: View,
        private val binding: SingleFavoriteChoiceViewBinding,
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
            binding.fFirstText.text = question.firstText
            setFirstStat(firstPercent, question.firstRate)
            binding.fLastText.text = question.lastText
            setLastStat(lastPercent, question.lastRate)
        }

        private fun setFirstStat(percentage: Int, amount: Int) {
            binding.fFirstPercentValue.text = percentage.toString()
            binding.fFirstPeoplesAmount.text = amount.toString()
        }

        private fun setLastStat(percentage: Int, amount: Int) {
            binding.fLastPercentValue.text = percentage.toString()
            binding.fLastPeoplesAmount.text = amount.toString()
        }

        override fun onClick(v: View) {
            listener.onItemClick(bindingAdapterPosition, true)
        }
    }
}