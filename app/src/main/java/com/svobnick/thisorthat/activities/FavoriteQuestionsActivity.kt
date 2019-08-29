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
import com.svobnick.thisorthat.adapters.FavoriteQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.FavoriteQuestionsPresenter
import com.svobnick.thisorthat.view.FavoriteQuestionsView
import javax.inject.Inject

class FavoriteQuestionsActivity : MvpAppCompatActivity(), FavoriteQuestionsView {

    @Inject
    lateinit var questionDao: QuestionDao
    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter
    lateinit var presenter: FavoriteQuestionsPresenter

    @ProvidePresenter
    fun provideFavoriteQuestionsPresenter(): FavoriteQuestionsPresenter {
        return FavoriteQuestionsPresenter(application as ThisOrThatApp, requestQueue)
    }

    lateinit var myQuestionsList: RecyclerView
    lateinit var adapter: FavoriteQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_questions)
        presenter.attachView(this)

        myQuestionsList = findViewById(R.id.favorite_questions_list)
        myQuestionsList.layoutManager = LinearLayoutManager(this)
        adapter = FavoriteQuestionsAdapter()
        myQuestionsList.adapter = adapter

        presenter.getFavoriteQuestions()
    }

    override fun setFavoriteQuestions(it: List<Question>) {
        adapter.setFavoriteQuestions(it)
    }

    override fun updateFavoriteQuestions() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}