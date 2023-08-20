package com.svobnick.thisorthat.view

import android.view.View
import com.svobnick.thisorthat.model.Question
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface ChoiceView : MvpView {

    fun onChoiceClick(choice: View)
    fun showError(errorMsg: String)
    fun setNewQuestion(question: Question)
    fun setResultToView(question: Question, favorite: Boolean)
    fun reportQuestion(selected: View)
    fun switchFavoriteQuestion()
    fun shareQuestion()
    fun openComments()
}