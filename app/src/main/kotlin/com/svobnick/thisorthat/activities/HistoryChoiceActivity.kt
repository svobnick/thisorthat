package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.moxy.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.fragments.ChoiceFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.HistoryChoicePresenter
import com.svobnick.thisorthat.view.HistoryChoiceView
import kotlinx.android.synthetic.main.activity_history_choice.*
import kotlinx.android.synthetic.main.fragment_choice.*
import kotlinx.android.synthetic.main.fragment_choice_menu.*
import javax.inject.Inject

class HistoryChoiceActivity : MvpAppCompatActivity(), HistoryChoiceView {
    private val ANALYTICS_SCREEN_NAME = "Question viewer"

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

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
            extras.get("status") as String,
            extras.get("choice") as String
        )
        (history_choice as ChoiceFragment).setResultToView(
            question,
            extras.getBoolean("isFavorite")
        )
    }

    override fun onStart() {
        super.onStart()
        val extras = intent.extras!!
        if (Question.Choices.MY_QUESTION == extras.get("choice")) {
            switch_favorite_button.setImageResource(R.drawable.icon_favorite_disabled)
            switch_favorite_button.setOnClickListener { }
        }
        report_button.setOnClickListener { }
        first_text.setOnClickListener { }
        last_text.setOnClickListener { }

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, ANALYTICS_SCREEN_NAME)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS,ANALYTICS_SCREEN_NAME)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}