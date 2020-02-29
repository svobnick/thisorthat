package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.adapters.FavoriteQuestionsAdapter
import com.svobnick.thisorthat.adapters.MyQuestionsAdapter
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter

class QuestionsListFragment(private val position: Int, val presenter: ProfilePresenter) :
    MvpAppCompatFragment() {
    lateinit var mAdapter: MyQuestionsAdapter
    lateinit var fAdapter: FavoriteQuestionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_questions_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val questionsList: RecyclerView = view.findViewById(R.id.questions_list)
        if (position == 0) {
            mAdapter = MyQuestionsAdapter()

            val linearLayoutManager = LinearLayoutManager(context)
            questionsList.layoutManager = linearLayoutManager
            mAdapter.setHasStableIds(true)
            questionsList.adapter = mAdapter

            val scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    // Triggered only when new data needs to be appended to the list
                    // Add whatever code is needed to append new items to the bottom of the list
                    presenter.getMyQuestions(page * presenter.MY_QUESTIONS_LIMIT)
                }
            }
            questionsList.addOnScrollListener(scrollListener)

            presenter.getMyQuestions(0)
        } else {
            fAdapter = FavoriteQuestionsAdapter()

            val linearLayoutManager = LinearLayoutManager(context)
            questionsList.layoutManager = linearLayoutManager
            fAdapter.setHasStableIds(true)
            questionsList.adapter = fAdapter

            val scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    // Triggered only when new data needs to be appended to the list
                    // Add whatever code is needed to append new items to the bottom of the list
                    presenter.getFavoriteQuestions(page * presenter.FAVORITE_QUESTIONS_LIMIT)
                }
            }
            questionsList.addOnScrollListener(scrollListener)

            presenter.getFavoriteQuestions(0)
        }

    }

    fun addQuestionsToList(questions: List<Question>) {
        if (position == 0) {
            mAdapter.addQuestions(questions)
        } else {
            fAdapter.addQuestions(questions)
        }
    }
}