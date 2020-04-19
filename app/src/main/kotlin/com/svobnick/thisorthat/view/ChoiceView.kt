package com.svobnick.thisorthat.view

import android.view.View
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.svobnick.thisorthat.model.Question

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ChoiceView : MvpView {

    fun onChoiceClick(choice: View)
    fun showError(errorMsg: String)
    fun setNewQuestion(question: Question)
    fun setResultToView(question: Question)
    fun reportQuestion(selected: View)
    fun switchFavoriteQuestion()
    fun shareQuestion()
    fun openComments()
}