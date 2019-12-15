package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MenuSecondLevelView : MvpView {

    fun allQuestionsHandler()
    fun favoriteQuestionsHandler()
    fun myQuestionsHandler()

}