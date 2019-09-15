package com.svobnick.thisorthat.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
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
import com.svobnick.thisorthat.fragments.BottomSheetDialog
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.view.ChoiceView
import com.svobnick.thisorthat.view.PieChart
import kotlinx.android.synthetic.main.activity_choice.*
import javax.inject.Inject
import kotlin.math.roundToInt

class ChoiceActivity : MvpAppCompatActivity(), ChoiceView {
    private val TAG = this::class.java.name

    lateinit var state: STATE
    lateinit var chart: PieChart
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
        this.chart = findViewById(R.id.pie_chart)
        this.popupWindow = setupPopupWindow()
        choicePresenter.setNextQuestion()

        setSupportActionBar(menu_bar)
    }

    fun onMenuButtonClick(selected: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val fragment = BottomSheetDialog()
                fragment.show(supportFragmentManager, fragment.tag)
            }
            R.id.add_favorite -> addFavoriteQuestion()
            R.id.comments -> getComments()
            R.id.report_problem -> reportQuestion()
            R.id.share -> println("top kek")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onChoiceClick(choice: View) {
        if (state == STATE.RESULT) {
            val clickedText = findViewById<TextView>(choice.id)
            choicePresenter.saveChoice(clickedText.text!!.toString())
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
        hideChart()
    }

    override fun setResultToView(question: Question) {
        val firstText = findViewById<TextView>(R.id.firstText)
        val secondText = findViewById<TextView>(R.id.secondText)
        firstText.text = question.firstRate.toString()
        secondText.text = question.secondRate.toString()
        setDataToChart(question.firstRate, question.secondRate)
    }

    fun onReportClickHandler(selected: View) {
        val reportReason = when (selected.id) {
            R.id.clone -> "clone"
            R.id.abuse -> "abuse"
            R.id.typo -> "typo"
            else -> ""
        }
        choicePresenter.reportQuestion(reportReason)
        // todo choicePresenter.saveChoice()
        showError("Вопрос пропущен, а его рейтинг снижен")
        popupWindow.dismiss()
        choicePresenter.setNextQuestion()
    }

    override fun reportQuestion() {
        val button = findViewById<Button>(R.id.push_button)
        popupWindow.showAtLocation(button, Gravity.CENTER, 0, 0)
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

    fun setupPopupWindow(): PopupWindow {
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

    private fun hideChart() {
        chart.visibility = View.INVISIBLE
        chart.invalidate()
    }

    fun setDataToChart(firstRate: Int, secondRate: Int) {
        val sum = firstRate + secondRate
        val firstPercent = ((firstRate.toDouble() / sum) * 100).roundToInt()
        val secondPercent = ((secondRate.toDouble() / sum) * 100).roundToInt()
        chart.setUpPercents(secondPercent)
        chart.visibility = View.VISIBLE
        chart.invalidate()
    }

    fun changeState() = if (state == STATE.QUESTION) STATE.RESULT else STATE.QUESTION

    enum class STATE {
        QUESTION,
        RESULT

    }
}
