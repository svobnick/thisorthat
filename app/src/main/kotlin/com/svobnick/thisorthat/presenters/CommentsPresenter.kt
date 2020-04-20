package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.service.addCommentRequest
import com.svobnick.thisorthat.service.getCommentsRequest
import com.svobnick.thisorthat.view.CommentsView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
                Response.Listener { response ->
                    Single.fromCallable {
                        val commentsJson =
                            (JSONObject(response)["result"] as JSONObject)["comments"] as JSONArray
                        val result = mutableListOf<Comment>()
                        for (i in 0 until commentsJson.length()) {
                            val commentJson = commentsJson.get(i) as JSONObject
                            result.add(
                                Comment(
                                    (commentJson["comment_id"] as String).toLong(),
                                    (commentJson["user_id"] as String).toLong(),
                                    (commentJson["parent"] as String).toLong(),
                                    (commentJson["text"] as String),
                                    (commentJson["avatar"] as String)
                                )
                            )
                        }
                        result
                    }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            viewState.setComments(it)
                        }, {
                            viewState.showError(it.localizedMessage)
                        })
                },
                Response.ErrorListener {
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    Log.e(TAG, errorMsg)
                    viewState.showError(errorMsg)
                }
            )
        )
    }

    fun addComment(text: String) {
        if (isValidComment(text)) {
            requestQueue.add(
                addCommentRequest(
                    app.authToken,
                    1.toString(),
                    text,
                    0.toString(), // 0 means that there are no parent
                    Response.Listener { response ->
                        print(response)
                        viewState.onCommentAdded()
                    },
                    Response.ErrorListener {
                        val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                        Log.e(TAG, errorMsg)
                        viewState.showError(errorMsg)
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