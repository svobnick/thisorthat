package com.svobnick.thisorthat.presenters

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.service.addCommentRequest
import com.svobnick.thisorthat.service.getCommentsRequest
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.CommentsView
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@InjectViewState
class CommentsPresenter(private val app: ThisOrThatApp) : MvpPresenter<CommentsView>() {
    private val TAG = this::class.java.name
    internal val LIMIT = 30L

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    fun getComments(questionId: Long, offset: Long) {
        requestQueue.add(
            getCommentsRequest(
                app.authToken,
                questionId.toString(),
                LIMIT.toString(),
                offset.toString(),
                Response.Listener {
                    val commentsJson = (JSONObject(it)["result"] as JSONObject)["comments"] as JSONArray
                    val result = mutableListOf<Comment>()
                    for (i in 0 until commentsJson.length()) {
                        val commentJson = commentsJson.get(i) as JSONObject
                        result.add(
                            Comment(
                                (commentJson["name"] as String),
                                (commentJson["comment_id"] as String).toLong(),
                                (commentJson["user_id"] as String).toLong(),
                                (commentJson["parent"] as String).toLong(),
                                (commentJson["message"] as String),
                                (commentJson["avatar"] as String)
                            )
                        )
                    }
                    if (result.size == 0) {
                        viewState.showEmptyComments()
                    } else {
                        viewState.setComments(result)
                    }
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                }
            )
        )
    }

    fun addComment(text: String) {
        if (isValidComment(text)) {
            requestQueue.add(
                addCommentRequest(
                    app.authToken,
                    4.toString(),
                    text,
                    0.toString(), // 0 means that there are no parent
                    Response.Listener { response ->
                        print(response)
                        viewState.onCommentAdded()
                    },
                    Response.ErrorListener {
                        ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                    }
                )

            )
        }
    }

    private fun isValidComment(text: String): Boolean {
        if ((text.isEmpty()) or (text.length > 300)) {
            viewState.showError("Текст комментария должен быть длиной 1-300 символов.")
            return false
        }
        return true
    }
}