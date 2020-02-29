package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.deleteFavoriteRequest
import com.svobnick.thisorthat.service.getFavoriteRequest
import com.svobnick.thisorthat.view.ProfileView
import org.json.JSONArray
import org.json.JSONObject

@InjectViewState
class ProfilePresenter(
    private val app: ThisOrThatApp,
    private val requestQueue: RequestQueue
): MvpPresenter<ProfileView>() {
    private val TAG = this::class.java.name
    internal val MY_QUESTIONS_LIMIT = 30L
    internal val FAVORITE_QUESTIONS_LIMIT = 100L

    fun getMyQuestions(offset: Long) {
        val json = JSONObject()
            .put("token", app.authToken)
            .put("limit", MY_QUESTIONS_LIMIT.toString())
            .put("offset", offset.toString())
        requestQueue.add(
            com.svobnick.thisorthat.service.getMyQuestions(
                json,
                Response.Listener { response ->
                    val items = (JSONObject(response)["result"] as JSONObject)["items"] as JSONArray
                    val questions = mutableListOf<Question>()
                    for (i in 0 until items.length()) {
                        val json = items.get(i) as JSONObject
                        questions.add(
                            Question(
                                (json["item_id"] as String).toLong(),
                                json["first_text"] as String,
                                json["last_text"] as String,
                                json["first_vote"] as Int,
                                json["last_vote"] as Int,
                                null
                            )
                        )
                    }
                    Log.i(TAG, "Receive my questions")
                    viewState.setQuestions(0, questions)
                },
                Response.ErrorListener {
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    Log.e(TAG, errorMsg)
                    viewState.showError(errorMsg)
                })
        )
    }

    fun getFavoriteQuestions(offset: Long) {
        val json = JSONObject()
            .put("token", app.authToken)
            .put("limit", FAVORITE_QUESTIONS_LIMIT.toString())
            .put("offset", offset.toString())
        requestQueue.add(
            getFavoriteRequest(
                json,
                Response.Listener { response ->
                    val items = (JSONObject(response)["result"] as JSONObject)["items"] as JSONArray
                    val questions = mutableListOf<Question>()
                    for (i in 0 until items.length()) {
                        val json = items.get(i) as JSONObject
                        questions.add(
                            Question(
                                (json["item_id"] as String).toLong(),
                                json["first_text"] as String,
                                json["last_text"] as String,
                                json["first_vote"] as Int,
                                json["last_vote"] as Int,
                                null
                            )
                        )
                    }
                    Log.i(TAG, "Receive my questions")
                    viewState.setQuestions(1, questions)
                },
                Response.ErrorListener {
                    val errData = JSONObject(String(it.networkResponse.data)).toString()
                    viewState.showError(errData)
                })
        )
    }

    fun deleteFavoriteQuestion(itemId: Long) {
        requestQueue.add(
            deleteFavoriteRequest(
                app.authToken,
                itemId.toString(),
                Response.Listener { response ->
                    Log.i(TAG, response.toString())
                },
                Response.ErrorListener {
                    val errData = JSONObject(String(it.networkResponse.data)).toString()
                    viewState.showError(errData)
                })
        )
    }
}