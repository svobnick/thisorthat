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
import com.svobnick.thisorthat.adapters.AnsweredQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.AnsweredQuestionsPresenter
import com.svobnick.thisorthat.view.AnsweredQuestionsView
import javax.inject.Inject

class AnsweredQuestionsActivity: MvpAppCompatActivity(), AnsweredQuestionsView {

    @Inject lateinit var questionDao: QuestionDao
    @Inject lateinit var requestQueue: RequestQueue

    @InjectPresenter
    lateinit var presenter: AnsweredQuestionsPresenter

    @ProvidePresenter
    fun provideAnsweredQuestionsPresenter(): AnsweredQuestionsPresenter {
        return AnsweredQuestionsPresenter(questionDao, requestQueue)
    }

    lateinit var answeredQuestionsList: RecyclerView
    lateinit var adapter: AnsweredQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answered_questions)
        presenter.attachView(this)

        answeredQuestionsList = findViewById(R.id.answered_questions_list)
        answeredQuestionsList.layoutManager = LinearLayoutManager(this)
        adapter = AnsweredQuestionsAdapter()
        answeredQuestionsList.adapter = adapter

        presenter.getAnsweredQuestions()
    }

    override fun setAnsweredQuestions(answeredQuestions: List<Question>) {
        adapter.setAnsweredQuestions(answeredQuestions)
    }

    override fun updateQuestions() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}