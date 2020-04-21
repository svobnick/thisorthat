package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.BottomMenuPresenter
import com.svobnick.thisorthat.view.BottomMenuView
import kotlinx.android.synthetic.main.fragment_bottom_menu.*

class BottomMenuFragment : MvpAppCompatFragment(), BottomMenuView {

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var presenter: BottomMenuPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideBottomMenuPresenter(): BottomMenuPresenter {
        return BottomMenuPresenter(
            activity!!.application as ThisOrThatApp
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bottom_menu, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        choice_button.setOnClickListener { switchFragment(0) }
        new_choice_button.setOnClickListener { switchFragment(1) }
        profile_button.setOnClickListener { switchFragment(2) }
    }

    fun switchFragment(menuNumber: Int) {
        goBackToMainScreen()
        presenter.switchFragment(menuNumber)
        updateUI(menuNumber)
    }

    override fun onResume() {
        super.onResume()
        updateUI(presenter.menuState.currentMenuItem)
    }

    override fun updateUI(menuNumber: Int) {
        when (menuNumber) {
            0 -> {
                choice_button.setImageResource(R.drawable.icon_choice)
                new_choice_button.setImageResource(R.drawable.icon_new_choice_disabled)
                profile_button.setImageResource(R.drawable.icon_profile_disabled)
            }
            1 -> {
                choice_button.setImageResource(R.drawable.icon_choice_disabled)
                new_choice_button.setImageResource(R.drawable.icon_new_choice)
                profile_button.setImageResource(R.drawable.icon_profile_disabled)
            }
            2 -> {
                choice_button.setImageResource(R.drawable.icon_choice_disabled)
                new_choice_button.setImageResource(R.drawable.icon_new_choice_disabled)
                profile_button.setImageResource(R.drawable.icon_profile)
            }
        }
    }

    private fun goBackToMainScreen() {
        if (activity is CommentsActivity) {
            activity!!.onBackPressed()
        }
        if (activity is HistoryChoiceActivity) {
            activity!!.onBackPressed()
        }
    }
}