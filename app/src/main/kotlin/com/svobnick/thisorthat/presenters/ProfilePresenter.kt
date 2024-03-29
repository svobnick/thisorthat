package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.getFavoriteRequest
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.ProfileView
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@InjectViewState
class ProfilePresenter(private val app: ThisOrThatApp) : MvpPresenter<ProfileView>() {
    private val TAG = this::class.java.name
    internal val MY_QUESTIONS_LIMIT = 30L
    internal val FAVORITE_QUESTIONS_LIMIT = 100L

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    fun getMyQuestions(offset: Long) {
        val requestJson = JSONObject()
            .put("token", app.authToken)
            .put("limit", MY_QUESTIONS_LIMIT.toString())
            .put("offset", offset.toString())
        requestQueue.add(
            com.svobnick.thisorthat.service.getMyQuestions(
                requestJson,
                {
                    val items = (JSONObject(it)["result"] as JSONObject)["items"] as JSONArray
                    if (items.length() == 0 && offset == 0L) {
                        viewState.showEmptyList(0)
                    } else {
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
                                    json["status"] as String,
                                    Question.Choices.MY_QUESTION
                                )
                            )
                        }
                        Log.i(TAG, "Receive my questions")
                        viewState.setQuestions(0, questions)
                    }
                },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    fun getFavoriteQuestions(offset: Long) {
        val requestJson = JSONObject()
            .put("token", app.authToken)
            .put("limit", FAVORITE_QUESTIONS_LIMIT.toString())
            .put("offset", offset.toString())
        requestQueue.add(
            getFavoriteRequest(
                requestJson,
                {
                    val items = (JSONObject(it)["result"] as JSONObject)["items"] as JSONArray
                    if (items.length() == 0 && offset == 0L) {
                        viewState.showEmptyList(1)
                    } else {
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
                                    json["status"] as String,
                                    Question.Choices.FAVORITE_QUESTION
                                )
                            )
                        }
                        Log.i(TAG, "Receive my questions")
                        viewState.setQuestions(1, questions)
                    }
                },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }
}