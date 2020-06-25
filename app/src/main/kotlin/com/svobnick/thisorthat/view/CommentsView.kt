package com.svobnick.thisorthat.view

import android.view.View
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.svobnick.thisorthat.model.Comment

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface CommentsView : MvpView {
    fun showEmptyComments()
    fun setComments(it: List<Comment>)
    fun addComment(sendView: View)
    fun onCommentAdded(comment: Comment)
    fun showError(errorMsg: String)
}