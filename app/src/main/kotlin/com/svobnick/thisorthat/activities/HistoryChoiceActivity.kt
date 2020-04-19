package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.moxy.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.fragments.ChoiceFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.HistoryChoicePresenter
import com.svobnick.thisorthat.view.HistoryChoiceView
import kotlinx.android.synthetic.main.activity_history_choice.*
import kotlinx.android.synthetic.main.fragment_choice.*

class HistoryChoiceActivity : MvpAppCompatActivity(), HistoryChoiceView {

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var hcPresenter: HistoryChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun providePresenter(): HistoryChoicePresenter {
        return HistoryChoicePresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_choice)
        hcPresenter.attachView(this)

        initFields()
    }

    private fun initFields() {
        report_button.visibility = INVISIBLE
        val extras = intent.extras!!
        val question = Question(
            extras.get("itemId") as Long,
            extras.get("firstText") as String,
            extras.get("lastText") as String,
            extras.get("firstRate") as Int,
            extras.get("lastRate") as Int,
            Question.HISTORY
        )
        (history_choice as ChoiceFragment).setResultToView(question)
        report_button.setOnClickListener(null)
        first_text.setOnClickListener(null)
        last_text.setOnClickListener(null)
    }
}