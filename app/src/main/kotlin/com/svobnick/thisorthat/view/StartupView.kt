package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface StartupView : MvpView {

    fun onStartupEnd()
    fun showError(errorMsg: String)
}