package com.svobnick.thisorthat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.view.ChoiceView
import javax.inject.Inject

class ChoiceActivity : MvpAppCompatActivity(), ChoiceView {

    @Inject lateinit var questionDao: QuestionDao
    @Inject lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var presenter: ChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideChoicePresenter(): ChoicePresenter {
        return ChoicePresenter(application as ThisOrThatApp, questionDao, requestQueue)
    }

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)

        presenter.setNextQuestion()
    }

    fun onMenuButtonClick(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    override fun makeChoice(choice: View) {
        val clickedText = findViewById<TextView>(choice.id)
        presenter.saveChoice(clickedText.text!!)
        presenter.setNextQuestion()
    }

    override fun setNewQuestion(question: Question) {
        val thisText = findViewById<TextView>(R.id.thisText)
        val thatText = findViewById<TextView>(R.id.thatText)
        thisText.text = question.firstText
        thatText.text = question.second
    }

    override fun skipQuestion() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}
