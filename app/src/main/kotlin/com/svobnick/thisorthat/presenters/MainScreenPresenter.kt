package com.svobnick.thisorthat.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.MenuInteractor
import com.svobnick.thisorthat.view.MainScreenView
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@InjectViewState
class MainScreenPresenter(private val application: ThisOrThatApp) : MvpPresenter<MainScreenView>() {

    @Inject
    lateinit var menuInteractor: MenuInteractor

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