package com.svobnick.thisorthat.view

import android.view.View
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface NewChoiceView : MvpView {

    fun onSendQuestionButtonClick(selected: View)
    fun showError(errorMsg: String)
    fun showSuccess()
}