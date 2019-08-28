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

    fun send(firstText: String, secondText: String) {
        val json = JSONObject()
            .put("first_text", firstText)
            .put("last_text", secondText)
            .put("token", app.authToken)
        if (app.authToken() != null) {
            requestQueue.add(
                sendNewQuestion(
                    json,
                    Response.Listener { response ->
                        Log.i(this::class.java.name, "$response")
                    },
                    Response.ErrorListener {
                        Log.e(this::class.java.name, String(it.networkResponse.data))
                    })
            )
        }
    }
}
