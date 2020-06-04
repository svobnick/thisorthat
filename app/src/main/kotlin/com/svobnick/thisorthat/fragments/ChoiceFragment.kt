package com.svobnick.thisorthat.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.moxy.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ChoicePresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.computeQuestionsPercentage
import com.svobnick.thisorthat.view.ChoiceView
import kotlinx.android.synthetic.main.fragment_choice.*
import kotlinx.android.synthetic.main.fragment_choice.view.*
import kotlinx.android.synthetic.main.fragment_choice_menu.*
import kotlinx.android.synthetic.main.popup_report_choice.view.*
import kotlinx.android.synthetic.main.popup_report_result.view.*


class ChoiceFragment : MvpAppCompatFragment(), ChoiceView {
    private val TAG = this::class.java.name

    private lateinit var state: STATE
    private lateinit var reportChoiceWindow: PopupWindow
    private lateinit var reportResultWindow: PopupWindow

    private lateinit var currentQuestion: Question
    private var isFavorite: Boolean = false

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
            currentQuestion.choice = if (clickedText.text.toString() == currentQuestion.firstText) Question.FIRST else Question.LAST
            choicePresenter.saveChoice(currentQuestion)
            setResultToView(currentQuestion, isFavorite)
        }

        state = changeState()
    }

    override fun setNewQuestion(question: Question) {
        currentQuestion = question
        first_text.text = currentQuestion.firstText
        last_text.text = currentQuestion.lastText
        isFavorite = false
        switch_favorite_button.setImageResource(R.drawable.icon_favorite_disabled)
        first_card_group.alpha = 1f
        first_text.alpha = 1f
        last_card_group.alpha = 1f
        last_text.alpha = 1f
        hideResults()
    }

    override fun setResultToView(question: Question, favorite: Boolean) {
        currentQuestion = question
        first_text.text = question.firstText
        last_text.text = question.lastText
        val firstRate = question.firstRate
        val lastRate = question.lastRate
        if (favorite) {
            switch_favorite_button.setImageResource(R.drawable.icon_favorite)
            isFavorite = true
        }
        val (firstPercent, lastPercent) = computeQuestionsPercentage(firstRate, lastRate)
        (childFragmentManager.findFragmentById(R.id.first_stat)!! as ChoiceStatFragment).setStat(
            firstPercent,
            firstRate,
            question.choice == Question.FIRST
        )
        (childFragmentManager.findFragmentById(R.id.last_stat)!! as ChoiceStatFragment).setStat(
            lastPercent,
            lastRate,
            question.choice == Question.LAST
        )
        if (question.choice == Question.FIRST) {
            last_card_group.alpha = 0.75f
            last_text.alpha = 0.75f
        } else {
            first_card_group.alpha = 0.75f
            first_text.alpha = 0.75f
        }

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
        reportResultWindow.showAtLocation(activity!!.findViewById(R.id.main_screen_root), Gravity.CENTER, 0, 0)
        dimBackground(activity!!, reportResultWindow.contentView.rootView)
    }

    override fun reportQuestion(selected: View) {
        reportChoiceWindow.showAtLocation(activity!!.findViewById(R.id.main_screen_root), Gravity.CENTER, 0, 0)
        dimBackground(activity!!, reportChoiceWindow.contentView.rootView)
    }

    private fun hideReportResult() {
        reportResultWindow.dismiss()
    }

    override fun openComments() {
        val intent = Intent(context, CommentsActivity::class.java)
        intent.putExtra("id", currentQuestion.id)
        intent.putExtra("firstText", currentQuestion.firstText)
        intent.putExtra("lastText", currentQuestion.lastText)
        intent.putExtra("firstRate", currentQuestion.firstRate.toString())
        intent.putExtra("lastRate", currentQuestion.lastRate.toString())
        val (firstPercent, lastPercent) = computeQuestionsPercentage(currentQuestion.firstRate, currentQuestion.lastRate)
        intent.putExtra("firstPercent", firstPercent.toString())
        intent.putExtra("lastPercent", lastPercent.toString())
        startActivity(intent)
    }

    override fun switchFavoriteQuestion() {
        if (isFavorite) {
            deleteFavoriteQuestion()
        } else {
            addFavoriteQuestion()
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
        val insertImage = MediaStore.Images.Media.insertImage(
            context?.contentResolver,
            getViewBitmap(choice_view),
            "То или Это",
            "thisorthat.ru"
        )

        val intent = Intent("com.instagram.share.ADD_TO_STORY")
        intent.setDataAndType(Uri.parse(insertImage),  "image/*");
        intent.putExtra("content_url", "https://thisorthat.ru")
        intent.putExtra("top_background_color", resources.getColor(R.color.gradient_start))
        intent.putExtra("bottom_background_color", resources.getColor(R.color.gradient_end))

        val activity: Activity? = activity
        activity?.grantUriPermission("com.instagram.android", Uri.parse(insertImage), Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            activity.startActivityForResult(intent, 0);
        }
    }

    private fun getViewBitmap(view: View): Bitmap {
        view.clearFocus()
        view.isPressed = false

        val willNotCache: Boolean = view.willNotCacheDrawing()
        view.setWillNotCacheDrawing(false)

        val color: Int = view.drawingCacheBackgroundColor
        view.drawingCacheBackgroundColor = 0

        if (color != 0) {
            view.destroyDrawingCache()
        }
        view.buildDrawingCache()
        val cacheBitmap: Bitmap = view.drawingCache

        val bitmap = Bitmap.createBitmap(cacheBitmap)

        view.destroyDrawingCache()
        view.setWillNotCacheDrawing(willNotCache)
        view.drawingCacheBackgroundColor = color

        return bitmap
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun setupReportPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(context)
        val reportView = LayoutInflater.from(context).inflate(R.layout.popup_report_choice, null)
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
        val responseView = LayoutInflater.from(context).inflate(R.layout.popup_report_result, null)
        popupWindow.contentView = responseView
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        responseView.report_result_ok.setOnClickListener { hideReportResult() }
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