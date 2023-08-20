package com.svobnick.thisorthat.presenters

import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.BottomMenuState
import com.svobnick.thisorthat.view.MainScreenView
import io.reactivex.rxjava3.disposables.Disposable
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class MainScreenPresenter(private val application: ThisOrThatApp) : MvpPresenter<MainScreenView>() {

    @Inject
    lateinit var menuInteractor: BottomMenuState

    private lateinit var disposable: Disposable

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        application.injector.inject(this)
        disposable = menuInteractor.switchedFragment.subscribe(viewState::switchFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}