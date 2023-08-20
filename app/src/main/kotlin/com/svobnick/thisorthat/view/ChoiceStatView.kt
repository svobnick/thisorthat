package com.svobnick.thisorthat.view

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ChoiceStatView : MvpView {

    fun setStat(percentage: Int, amount: Int, userChoice: Boolean)
}