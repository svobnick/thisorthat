package com.svobnick.thisorthat.view

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ChoiceMenuView : MvpView {

    fun commentsHandler()
    fun switchFavoriteHandler()
    fun shareHandler()
}