package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.AnsweredQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.AnsweredQuestionsPresenter
import com.svobnick.thisorthat.view.AnsweredQuestionsView
import kotlinx.android.synthetic.main.activity_answered_questions.*
import javax.inject.Inject

class AnsweredQuestionsActivity : MvpAppCompatActivity(), AnsweredQuestionsView {
    private val adapter = AnsweredQuestionsAdapter()

    @Inject
    lateinit var questionDao: QuestionDao

    @InjectPresenter
    lateinit var presenter: AnsweredQuestionsPresenter

    @ProvidePresenter
    fun provideAnsweredQuestionsPresenter(): AnsweredQuestionsPresenter {
        return AnsweredQuestionsPresenter(questionDao)
    }

    private lateinit var answeredQuestionsList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answered_questions)
        presenter.attachView(this)

        answeredQuestionsList = answered_questions_list
        answeredQuestionsList.layoutManager = LinearLayoutManager(this)
        adapter.setHasStableIds(true)
        answeredQuestionsList.adapter = adapter

        presenter.getAnsweredQuestions()
    }

    override fun setAnsweredQuestions(it: List<Question>) {
        adapter.setAnsweredQuestions(it)
    }

    // todo add swipe-to-refresh to update the list
    override fun updateQuestions() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}