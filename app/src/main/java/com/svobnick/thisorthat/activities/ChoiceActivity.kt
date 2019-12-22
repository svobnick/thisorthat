package com.svobnick.thisorthat.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
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
import com.svobnick.thisorthat.fragments.ChoiceStatFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.ChoiceView
import kotlinx.android.synthetic.main.activity_choice.*
import javax.inject.Inject

class ChoiceActivity : MvpAppCompatActivity(), ChoiceView {
    private val TAG = this::class.java.name

    lateinit var state: STATE
    lateinit var firstPercent: ChoiceStatFragment
    lateinit var lastPercent: ChoiceStatFragment
    lateinit var popupWindow: PopupWindow

    @Inject
    lateinit var questionDao: QuestionDao
    @Inject
    lateinit var answerDao: AnswerDao
    @Inject
    lateinit var claimDao: ClaimDao
    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var choicePresenter: ChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideChoicePresenter(): ChoicePresenter {
        return ChoicePresenter(
            application as ThisOrThatApp,
            questionDao,
            answerDao,
            claimDao,
            requestQueue
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        choicePresenter.attachView(this)

        this.state = STATE.QUESTION
        val fragmentManager = supportFragmentManager
        this.firstPercent = fragmentManager.findFragmentById(R.id.first_stat) as ChoiceStatFragment
        this.lastPercent = fragmentManager.findFragmentById(R.id.last_stat) as ChoiceStatFragment
        this.popupWindow = setupPopupWindow()
        choicePresenter.setNextQuestion()
    }

    override fun onChoiceClick(choice: View) {
        if (state == STATE.RESULT) {
            choicePresenter.setNextQuestion()
        } else {
            val clickedText = findViewById<TextView>(choice.id)
            val userChoice = choicePresenter.saveChoice(clickedText.text.toString())
            setResultToView(choicePresenter.currentQuestion, userChoice)
        }

        state = changeState()
    }

    override fun setNewQuestion(question: Question) {
        val thisText = first_text
        val thatText = last_text
        thisText.text = question.firstText
        thatText.text = question.lastText
        hideResults()
    }

    override fun setResultToView(question: Question, userChoice: String) {
        val firstRate = question.firstRate
        val lastRate = question.lastRate
        val (firstPercent, lastPercent) = computeQuestionsPercentage(firstRate, lastRate)
        this.firstPercent.setStat(firstPercent, firstRate, userChoice == Question.FIRST)
        this.lastPercent.setStat(lastPercent, lastRate, userChoice == Question.LAST)
        showResults()
    }

    fun onReportClickHandler(selected: View) {
        val reportReason = when (selected.id) {
            R.id.clone -> "clone"
            R.id.abuse -> "abuse"
            R.id.typo -> "typo"
            else -> throw IllegalArgumentException("type ${selected.id} is not allowed here")
        }
        choicePresenter.reportQuestion(reportReason)
        showError("Вопрос пропущен, а его рейтинг снижен")
        popupWindow.dismiss()
        choicePresenter.setNextQuestion()
    }

    override fun reportQuestion(view: View) {
        popupWindow.showAtLocation(or_button, Gravity.CENTER, 0, 0)
    }

    override fun getComments() {
        val intent = Intent(this, CommentsActivity::class.java)
        startActivity(intent)
    }

    override fun addFavoriteQuestion() {
        choicePresenter.addFavoriteQuestion()
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun setupPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(this)
        val reportView = LayoutInflater.from(this).inflate(R.layout.report_view, null)
        popupWindow.contentView = reportView
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        return popupWindow
    }

    private fun hideResults() {
        supportFragmentManager.beginTransaction()
            .hide(firstPercent)
            .hide(lastPercent)
            .commit()
    }

    private fun showResults() {
        supportFragmentManager.beginTransaction()
            .show(firstPercent)
            .show(lastPercent)
            .commit()
    }

    private fun changeState() = if (state == STATE.QUESTION) STATE.RESULT else STATE.QUESTION

    enum class STATE {
        QUESTION,
        RESULT
    }
}
