package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.sendNewQuestion
import com.svobnick.thisorthat.view.NewQuestionView
import org.json.JSONObject

@InjectViewState
class NewQuestionPresenter(
    val app: ThisOrThatApp,
    val requestQueue: RequestQueue
) : MvpPresenter<NewQuestionView>() {
    private val TAG = this::class.java.name

    fun send(firstText: String, lastText: String) {
        val json = JSONObject()
            .put("first_text", firstText)
            .put("last_text", lastText)
            .put("token", app.authToken)
        requestQueue.add(
            sendNewQuestion(
                json,
                Response.Listener { response ->
                    Log.i(TAG, response)
                },
                Response.ErrorListener {
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    Log.e(TAG, errorMsg)
                    viewState.showError(errorMsg)
                })
        )
    }
}
