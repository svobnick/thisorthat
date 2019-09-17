package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.adapters.MyQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.MyQuestionsPresenter
import com.svobnick.thisorthat.view.MyQuestionsView
import kotlinx.android.synthetic.main.activity_my_questions.*
import javax.inject.Inject

class MyQuestionsActivity : MvpAppCompatActivity(), MyQuestionsView {
    private val adapter = MyQuestionsAdapter()

    @Inject
    lateinit var questionDao: QuestionDao
    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter
    lateinit var presenter: MyQuestionsPresenter

    @ProvidePresenter
    fun provideMyQuestionsPresenter(): MyQuestionsPresenter {
        return MyQuestionsPresenter(application as ThisOrThatApp, requestQueue)
    }

    private lateinit var myQuestionsList: RecyclerView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_questions)
        presenter.attachView(this)

        myQuestionsList = my_questions_list
        val linearLayoutManager = LinearLayoutManager(this)
        myQuestionsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        myQuestionsList.adapter = adapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                presenter.getMyQuestions(page * presenter.LIMIT)
            }
        }
        myQuestionsList.addOnScrollListener(scrollListener)

        presenter.getMyQuestions(0L)
    }

    override fun setMyQuestions(it: List<Question>) {
        adapter.setMyQuestions(it)
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}