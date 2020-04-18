package com.svobnick.thisorthat.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.MenuInteractor
import com.svobnick.thisorthat.view.BottomMenuView
import javax.inject.Inject

@InjectViewState
class BottomMenuPresenter(private val application: ThisOrThatApp) : MvpPresenter<BottomMenuView>() {

    @Inject
    lateinit var menuInteractor: MenuInteractor

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        application.injector.inject(this)
    }

    fun switchFragment(fragmentNumber: Int) {
        menuInteractor.switchFragment(fragmentNumber)
    }
}