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
import com.svobnick.thisorthat.adapters.MyQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.MyQuestionsPresenter
import com.svobnick.thisorthat.view.MyQuestionsView
import javax.inject.Inject

class MyQuestionsActivity : MvpAppCompatActivity(), MyQuestionsView {

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

    lateinit var myQuestionsList: RecyclerView
    lateinit var adapter: MyQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_questions)
        presenter.attachView(this)

        myQuestionsList = findViewById(R.id.my_questions_list)
        myQuestionsList.layoutManager = LinearLayoutManager(this)
        adapter = MyQuestionsAdapter()
        myQuestionsList.adapter = adapter

        presenter.getMyQuestions()
    }

    override fun setMyQuestions(it: List<Question>) {
        adapter.setMyQuestions(it)
    }

    override fun updateMyQuestions() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}