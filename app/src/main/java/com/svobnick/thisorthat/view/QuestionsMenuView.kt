package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface QuestionsMenuView : MvpView {

    fun allQuestionsHandler()
    fun favoriteQuestionsHandler()
    fun myQuestionsHandler()

}