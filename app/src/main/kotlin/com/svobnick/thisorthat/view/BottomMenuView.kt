package com.svobnick.thisorthat.view

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface BottomMenuView : MvpView {
    fun updateUI(menuNumber: Int)
}