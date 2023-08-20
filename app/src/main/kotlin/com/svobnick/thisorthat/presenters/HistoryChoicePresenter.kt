package com.svobnick.thisorthat.presenters

import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.view.HistoryChoiceView
import moxy.InjectViewState
import moxy.MvpPresenter

@InjectViewState
class HistoryChoicePresenter(val app: ThisOrThatApp) : MvpPresenter<HistoryChoiceView>()  {
    private val TAG = this::class.java.name

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

}