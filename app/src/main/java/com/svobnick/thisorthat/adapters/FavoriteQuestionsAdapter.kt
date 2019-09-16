package com.svobnick.thisorthat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.model.Question

class FavoriteQuestionsAdapter :
    RecyclerView.Adapter<FavoriteQuestionsAdapter.FavoriteQuestionsViewHolder>() {
    private val favoriteQuestionsList = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteQuestionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_question_view, parent, false)
        return FavoriteQuestionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favoriteQuestionsList.size
    }

    override fun onBindViewHolder(holder: FavoriteQuestionsViewHolder, position: Int) {
        holder.bind(favoriteQuestionsList[position])
    }

    override fun getItemId(position: Int): Long {
        return favoriteQuestionsList[position].id
    }

    class FavoriteQuestionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var firstText: TextView = view.findViewById(R.id.first_text)
        private var secondText: TextView = view.findViewById(R.id.last_text)
        private var hiddenId: TextView = view.findViewById(R.id.hidden_id)

        fun bind(question: Question) {
            firstText.text = question.firstText
            secondText.text = question.secondText
            hiddenId.text = question.id.toString()
        }
    }

    fun setFavoriteQuestions(favoriteQuestions: List<Question>) {
        favoriteQuestionsList.addAll(favoriteQuestions)
        notifyDataSetChanged()
    }
}