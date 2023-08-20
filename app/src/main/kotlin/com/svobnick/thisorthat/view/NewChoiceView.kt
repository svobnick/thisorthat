package com.svobnick.thisorthat.view

import android.view.View
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface NewChoiceView : MvpView {

    fun onSendQuestionButtonClick(selected: View)
    fun onSuccessfullyAdded()
    fun onChoiceAlreadyExist(cloneId: String)
    fun showError(errorMsg: String)
}