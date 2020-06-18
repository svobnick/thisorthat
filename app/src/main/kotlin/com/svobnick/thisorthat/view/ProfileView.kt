package com.svobnick.thisorthat.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.svobnick.thisorthat.model.Question

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ProfileView : MvpView {

    fun setQuestions(position: Int, questions: List<Question>)

    fun showEmptyList(position: Int)

    fun showError(errorMsg: String)
}