package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.svobnick.thisorthat.model.Question

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface MyQuestionsView : MvpView {

    fun setMyQuestions(it: List<Question>)
    fun updateMyQuestions()
    fun showError(errorMsg: String)
}