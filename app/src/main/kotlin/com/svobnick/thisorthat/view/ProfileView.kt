package com.svobnick.thisorthat.view

import com.svobnick.thisorthat.model.Question
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ProfileView : MvpView {

    fun setQuestions(position: Int, questions: List<Question>)

    fun showEmptyList(position: Int)

    fun showError(errorMsg: String)
}