package com.svobnick.thisorthat.view

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface StartupView : MvpView {

    fun onStartupEnd()
    fun showError(errorMsg: String)
}