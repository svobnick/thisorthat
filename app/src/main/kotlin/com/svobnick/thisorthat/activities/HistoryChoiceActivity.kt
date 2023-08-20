package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View.INVISIBLE
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.ActivityHistoryChoiceBinding
import com.svobnick.thisorthat.databinding.FragmentChoiceBinding
import com.svobnick.thisorthat.databinding.FragmentChoiceMenuBinding
import com.svobnick.thisorthat.fragments.ChoiceFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.HistoryChoicePresenter
import com.svobnick.thisorthat.view.HistoryChoiceView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class HistoryChoiceActivity : MvpAppCompatActivity(), HistoryChoiceView {
    private val ANALYTICS_SCREEN_NAME = "Question viewer"

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @InjectPresenter
    lateinit var hcPresenter: HistoryChoicePresenter

    private lateinit var binding: ActivityHistoryChoiceBinding
    private lateinit var choiceBinding: FragmentChoiceBinding
    private lateinit var menuBinding: FragmentChoiceMenuBinding

    @ProvidePresenter
    fun providePresenter(): HistoryChoicePresenter {
        return HistoryChoicePresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryChoiceBinding.inflate(layoutInflater)
        choiceBinding = FragmentChoiceBinding.bind(binding.historyChoice)
        menuBinding = FragmentChoiceMenuBinding.bind(choiceBinding.choiceMenu)
        setContentView(binding.root)
        hcPresenter.attachView(this)

        initFields()
    }

    private fun initFields() {
        val choiceBinding = FragmentChoiceBinding.inflate(layoutInflater)
        choiceBinding.reportButton.visibility = INVISIBLE
        val extras = intent.extras!!
        val question = Question(
            extras.getLong("itemId"),
            extras.getString("firstText")!!,
            extras.getString("lastText")!!,
            extras.getInt("firstRate"),
            extras.getInt("lastRate"),
            extras.getString("status")!!,
            extras.getString("choice")!!
        )
        (supportFragmentManager.findFragmentById(binding.historyChoice.id) as ChoiceFragment).setResultToView(
            question,
            extras.getBoolean("isFavorite")
        )
    }

    override fun onStart() {
        super.onStart()
        val extras = intent.extras!!
        if (Question.Choices.MY_QUESTION == extras.getString("choice")) {
            menuBinding.switchFavoriteButton.setImageResource(R.drawable.icon_favorite_disabled)
            menuBinding.switchFavoriteButton.setOnClickListener { }
        }
        choiceBinding.reportButton.setOnClickListener { }
        choiceBinding.firstText.setOnClickListener { }
        choiceBinding.lastText.setOnClickListener { }

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, ANALYTICS_SCREEN_NAME)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS,ANALYTICS_SCREEN_NAME)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}