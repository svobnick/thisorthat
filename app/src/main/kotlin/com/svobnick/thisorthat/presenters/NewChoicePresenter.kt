package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.sendNewQuestion
import com.svobnick.thisorthat.view.NewChoiceView
import org.json.JSONObject
import javax.inject.Inject

@InjectViewState
class NewChoicePresenter(val app: ThisOrThatApp) : MvpPresenter<NewChoiceView>() {
    private val TAG = this::class.java.name

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    fun send(firstText: String, lastText: String) {
        if (isValidChoice(firstText, lastText)) {
            val json = JSONObject()
                .put("first_text", firstText)
                .put("last_text", lastText)
                .put("token", app.authToken)
            requestQueue.add(
                sendNewQuestion(
                    json,
                    Response.Listener { response ->
                        Log.i(TAG, response)
                        viewState.onSuccessfullyAdded()
                    },
                    Response.ErrorListener {
                        val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                        Log.e(TAG, errorMsg)
                        viewState.showError(errorMsg)
                    })
            )
        }
    }

    private fun isValidChoice(firstText: String, lastText: String): Boolean {
        if ((firstText.length < 4) or (lastText.length < 4)) {
            viewState.showError("Оба варианта должны быть длиной от 4 до 150 символов")
            return false
        }
        return true
    }
}
