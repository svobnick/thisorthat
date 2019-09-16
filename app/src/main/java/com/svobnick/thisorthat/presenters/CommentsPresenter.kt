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

@InjectViewState
class CommentsPresenter(
    private val app: ThisOrThatApp,
    private val requestQueue: RequestQueue
) : MvpPresenter<CommentsView>() {
    private val TAG = this::class.java.name
    val LIMIT = 30L

    fun getComments(questionId: Long, offset: Long) {
        requestQueue.add(
            getCommentsRequest(
                app.authToken!!,
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
                    Log.e(TAG, JSONObject(String(it.networkResponse.data)).toString())
                }
            )
        )
    }

    fun addComment(text: String) {
        requestQueue.add(
            addCommentRequest(
                app.authToken!!,
                4.toString(),
                text,
                0.toString(),
                Response.Listener { response ->
                    print(response)
                },
                Response.ErrorListener {
                    Log.e(TAG, JSONObject(String(it.networkResponse.data)).toString())
                }
            )

        )
    }
}