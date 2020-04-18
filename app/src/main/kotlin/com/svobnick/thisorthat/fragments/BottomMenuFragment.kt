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

        choice_button.setOnClickListener { presenter.switchFragment(0) }
        add_choice_button.setOnClickListener { presenter.switchFragment(1) }
        profile_button.setOnClickListener { presenter.switchFragment(2) }
    }

    override fun switchFragment(menuNumber: Int) {
        presenter.switchFragment(menuNumber)
    }
}