package com.svobnick.thisorthat.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.moxy.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.ChoiceView
import kotlinx.android.synthetic.main.fragment_choice.*
import kotlinx.android.synthetic.main.fragment_choice.view.*
import kotlinx.android.synthetic.main.fragment_choice_menu.*
import kotlinx.android.synthetic.main.report_result.view.*
import kotlinx.android.synthetic.main.report_view.view.*

class ChoiceFragment : MvpAppCompatFragment(), ChoiceView {
    private val TAG = this::class.java.name

    private lateinit var state: STATE
    private lateinit var reportChoiceWindow: PopupWindow
    private lateinit var reportResultWindow: PopupWindow

    private lateinit var currentQuestion: Question

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var choicePresenter: ChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideChoicePresenter(): ChoicePresenter {
        return ChoicePresenter(activity!!.application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity!!.application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        choicePresenter.attachView(this)

        this.state = STATE.QUESTION

        val view = inflater.inflate(R.layout.fragment_choice, container, false)
        view.first_text.setOnClickListener(this::onChoiceClick)
        view.last_text.setOnClickListener(this::onChoiceClick)
        view.report_button.setOnClickListener(this::reportQuestion)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.reportChoiceWindow = setupReportPopupWindow()
        this.reportResultWindow = setupResponsePopupWindow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        choicePresenter.setNextQuestion()
    }

    override fun onChoiceClick(choice: View) {
        if (state == STATE.RESULT) {
            choicePresenter.setNextQuestion()
        } else {
            val clickedText = activity!!.findViewById<TextView>(choice.id)
            currentQuestion.choice = if (clickedText.text.toString() == currentQuestion.firstText) currentQuestion.firstText else currentQuestion.lastText
            val userChoice = choicePresenter.saveChoice(currentQuestion)
            setResultToView(currentQuestion, userChoice)
        }

        state = changeState()
    }

    override fun setNewQuestion(question: Question) {
        currentQuestion = question
        first_text.text = currentQuestion.firstText
        last_text.text = currentQuestion.lastText
        switch_favorite_button.setImageResource(R.drawable.icon_favorite_disabled)
        hideResults()
    }

    override fun setResultToView(question: Question, userChoice: String) {
        val firstRate = question.firstRate
        val lastRate = question.lastRate
        val (firstPercent, lastPercent) = computeQuestionsPercentage(firstRate, lastRate)
        (childFragmentManager.findFragmentById(R.id.first_stat)!! as ChoiceStatFragment).setStat(
            firstPercent,
            firstRate,
            userChoice == Question.FIRST
        )
        (childFragmentManager.findFragmentById(R.id.last_stat)!! as ChoiceStatFragment).setStat(
            lastPercent,
            lastRate,
            userChoice == Question.LAST
        )
        showResults()
    }

    private fun onReportClickHandler(selected: View) {
        val reportReason = when (selected.id) {
            R.id.clone -> "clone"
            R.id.abuse -> "abuse"
            R.id.typo -> "typo"
            else -> throw IllegalArgumentException("type ${selected.id} is not allowed here")
        }
        choicePresenter.reportQuestion(currentQuestion, reportReason)
        reportResult()
        choicePresenter.setNextQuestion()
    }

    private fun reportResult() {
        reportChoiceWindow.dismiss()
        reportResultWindow.showAtLocation(or_button, Gravity.CENTER, 0, 0)
        dimBackground(reportResultWindow.contentView.rootView)
    }

    override fun reportQuestion(selected: View) {
        reportChoiceWindow.showAtLocation(or_button, Gravity.CENTER, 0, 0)
        dimBackground(reportChoiceWindow.contentView.rootView)
    }

    private fun hideReportResult() {
        reportResultWindow.dismiss()
    }

    private fun dimBackground(container: View) {
        val wm = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.7f
        wm.updateViewLayout(container, p)
    }


    override fun getComments() {
        val intent = Intent(context, CommentsActivity::class.java)
        startActivity(intent)
    }

    override fun switchFavoriteQuestion() {
        if (switch_favorite_button.drawable.constantState == getDrawable(context!!, R.drawable.icon_favorite_disabled)!!.constantState) {
            addFavoriteQuestion()
        } else {
            deleteFavoriteQuestion()
        }
    }

    private fun addFavoriteQuestion() {
        choicePresenter.addFavoriteQuestion(currentQuestion.id.toString())
        switch_favorite_button.setImageResource(R.drawable.icon_favorite)
    }

    private fun deleteFavoriteQuestion() {
        choicePresenter.deleteFavoriteQuestion(currentQuestion.id.toString())
        switch_favorite_button.setImageResource(R.drawable.icon_favorite_disabled)
    }

    override fun shareQuestion() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "thisorthat.ru")
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentQuestion.toString())
        startActivity(Intent.createChooser(shareIntent, "Поделиться вопросом!"))
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun setupReportPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(context)
        val reportView = LayoutInflater.from(context).inflate(R.layout.report_view, null)
        popupWindow.contentView = reportView
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        reportView.typo.setOnClickListener(this::onReportClickHandler)
        reportView.abuse.setOnClickListener(this::onReportClickHandler)
        reportView.clone.setOnClickListener(this::onReportClickHandler)
        return popupWindow
    }

    private fun setupResponsePopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(context)
        val responseView = LayoutInflater.from(context).inflate(R.layout.report_result, null)
        popupWindow.contentView = responseView
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        responseView.report_result_ok.setOnClickListener{ hideReportResult() }
        return popupWindow
    }

    private fun hideResults() {
        childFragmentManager.beginTransaction()
            .hide(childFragmentManager.findFragmentById(R.id.first_stat)!!)
            .hide(childFragmentManager.findFragmentById(R.id.last_stat)!!)
            .commit()
    }

    private fun showResults() {
        childFragmentManager.beginTransaction()
            .show(childFragmentManager.findFragmentById(R.id.first_stat)!!)
            .show(childFragmentManager.findFragmentById(R.id.last_stat)!!)
            .commit()
    }

    private fun changeState() = if (state == STATE.QUESTION) STATE.RESULT else STATE.QUESTION

    enum class STATE {
        QUESTION,
        RESULT
    }
}