package com.svobnick.thisorthat.adapters

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.favorite_question_single_view.*

class FavoriteQuestionsAdapter : RecyclerView.Adapter<FavoriteQuestionsAdapter.QuestionListViewHolder>(), QuestionsListAdapter {

    private val myQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_question_single_view, parent, false)
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

    override fun addQuestions(questions: List<Question>) {
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
            f_first_text.text = question.firstText
            setFirstStat(firstPercent, question.firstRate, true)
            f_last_text.text = question.lastText
            setLastStat(lastPercent, question.lastRate, true)
        }

        fun setFirstStat(percentage: Int, amount: Int, userChoice: Boolean) {
            val percentageAnim = ValueAnimator.ofInt(0, percentage)
            percentageAnim.duration = 500
            percentageAnim.addUpdateListener {
                f_first_percent_value.text = it.animatedValue.toString()
            }

            val amountAnim = ValueAnimator.ofInt(0, amount)
            amountAnim.duration = 500
            amountAnim.addUpdateListener {
                f_first_peoples_amount.text = it.animatedValue.toString()
            }

            if (!userChoice) {
                f_first_percent_value.alpha = 0.25f
                f_first_percent_symbol.alpha = 0.25f
                f_first_peoples_amount.alpha = 0.25f
            } else {
                f_first_percent_value.alpha = 1f
                f_first_percent_symbol.alpha = 1f
                f_first_peoples_amount.alpha = 1f
            }

            percentageAnim.start()
            amountAnim.start()
        }

        fun setLastStat(percentage: Int, amount: Int, userChoice: Boolean) {
            val percentageAnim = ValueAnimator.ofInt(0, percentage)
            percentageAnim.duration = 500
            percentageAnim.addUpdateListener {
                f_last_percent_value.text = it.animatedValue.toString()
            }

            val amountAnim = ValueAnimator.ofInt(0, amount)
            amountAnim.duration = 500
            amountAnim.addUpdateListener {
                f_last_peoples_amount.text = it.animatedValue.toString()
            }

            if (!userChoice) {
                f_last_percent_value.alpha = 0.25f
                f_last_percent_symbol.alpha = 0.25f
                f_last_peoples_amount.alpha = 0.25f
            } else {
                f_last_percent_value.alpha = 1f
                f_last_percent_symbol.alpha = 1f
                f_last_peoples_amount.alpha = 1f
            }

            percentageAnim.start()
            amountAnim.start()
        }
    }
}