package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.svobnick.thisorthat.model.Question

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface FavoriteQuestionsView : MvpView {

    fun setFavoriteQuestions(it: List<Question>)
    fun updateFavoriteQuestions()
    fun deleteFavoriteQuestion(itemId: Long)
    fun showError(errorMsg: String)
}