package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.moxy.MvpAppCompatFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.adapters.FavoriteQuestionsAdapter
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter
import com.svobnick.thisorthat.view.OnItemClickListener

class FavoriteQuestionsListFragment(val presenter: ProfilePresenter) : MvpAppCompatFragment(),
    OnItemClickListener {
    lateinit var adapter: FavoriteQuestionsAdapter
    private lateinit var questionsList: RecyclerView
    private lateinit var emptyListText: TextView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_questions_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        questionsList = view.findViewById(R.id.questions_list)
        emptyListText = view.findViewById(R.id.empty_question_list)
        adapter = FavoriteQuestionsAdapter(this)

        val linearLayoutManager = LinearLayoutManager(context)
        questionsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        questionsList.adapter = adapter

        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                presenter.getFavoriteQuestions(page * presenter.FAVORITE_QUESTIONS_LIMIT)
            }
        }
        questionsList.addOnScrollListener(scrollListener)
    }

    override fun onStart() {
        super.onStart()
        presenter.getFavoriteQuestions(0)
    }

    override fun onStop() {
        super.onStop()
        adapter.clear()
        scrollListener.resetState()
    }

    fun addQuestionsToList(questions: List<Question>) {
        adapter.addQuestions(questions)
    }

    override fun onItemClick(position: Int, favorite: Boolean) {
        val item = adapter.getItem(position)
        val intent = Intent(context, HistoryChoiceActivity::class.java)
        intent.putExtra("itemId", item.id)
        intent.putExtra("firstText", item.firstText)
        intent.putExtra("lastText", item.lastText)
        intent.putExtra("firstRate", item.firstRate)
        intent.putExtra("lastRate", item.lastRate)
        intent.putExtra("status", item.status)
        intent.putExtra("isFavorite", favorite)
        startActivity(intent)
    }

    fun showEmptyList() {
        questionsList.visibility = View.GONE
        emptyListText.visibility = View.VISIBLE
    }
}