package com.svobnick.thisorthat.activities

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
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.view.ChoiceView
import javax.inject.Inject

class ChoiceActivity : MvpAppCompatActivity(), ChoiceView {
    lateinit var state: STATE

    @Inject lateinit var questionDao: QuestionDao
    @Inject lateinit var answerDao: AnswerDao
    @Inject lateinit var claimDao: ClaimDao
    @Inject lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var choicePresenter: ChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideChoicePresenter(): ChoicePresenter {
        return ChoicePresenter(application as ThisOrThatApp, questionDao, answerDao, claimDao, requestQueue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        choicePresenter.attachView(this)

        this.state = STATE.QUESTION
        choicePresenter.setNextQuestion()
    }

    fun onMenuButtonClick(selected: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    override fun onChoiceClick(choice: View) {
        if (state == STATE.RESULT) {
            val clickedText = findViewById<TextView>(choice.id)
            choicePresenter.saveChoice(clickedText.text!!)
            choicePresenter.setNextQuestion()
        } else {
            setResultToView(choicePresenter.currentQuestion)
        }

        state = changeState()
    }

    override fun setNewQuestion(question: Question) {
        val thisText = findViewById<TextView>(R.id.firstText)
        val thatText = findViewById<TextView>(R.id.secondText)
        thisText.text = question.firstText
        thatText.text = question.secondText
    }

    override fun setResultToView(question: Question) {
        val firstText = findViewById<TextView>(R.id.firstText)
        val secondText = findViewById<TextView>(R.id.secondText)
        firstText.text = question.firstRate.toString()
        secondText.text = question.secondRate.toString()
    }

    override fun reportQuestion(selected: View) {
        choicePresenter.claimQuestion("clone")
        choicePresenter.setNextQuestion()
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }

    fun changeState() = if (state == STATE.QUESTION) STATE.RESULT else STATE.QUESTION

    enum class STATE {
        QUESTION,
        RESULT;

    }
}
