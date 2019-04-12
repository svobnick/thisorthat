package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.sendNewQuestion
import com.svobnick.thisorthat.view.NewQuestionView
import org.json.JSONArray
import org.json.JSONObject

@InjectViewState
class NewQuestionPresenter(
    val app: ThisOrThatApp,
    val requestQueue: RequestQueue
) : MvpPresenter<NewQuestionView>() {

    fun send(firstText: String, secondText: String) {
        val json = JSONObject()
        val items = JSONArray()
            .put(
                JSONObject()
                    .put("left_text", firstText)
                    .put("right_text", secondText)
            )
        json.put("items", items)
        if (app.authToken() != null) {
            requestQueue.add(
                sendNewQuestion(app.authToken()!!,
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
