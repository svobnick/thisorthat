package com.svobnick.thisorthat.view

import android.view.View
import com.svobnick.thisorthat.model.Comment
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = OneExecutionStateStrategy::class)
interface CommentsView : MvpView {
    fun showEmptyComments()
    fun setComments(it: List<Comment>)
    fun addComment(sendView: View)
    fun onCommentAdded(comment: Comment)
    fun showError(errorMsg: String)
}