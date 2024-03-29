package com.svobnick.thisorthat.presenters

import com.android.volley.RequestQueue
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.addFavoriteRequest
import com.svobnick.thisorthat.service.sendNewQuestion
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.NewChoiceView
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONObject
import javax.inject.Inject

@InjectViewState
class NewChoicePresenter(val app: ThisOrThatApp) : MvpPresenter<NewChoiceView>() {

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
                    { viewState.onSuccessfullyAdded() },
                    {
                        if (it.networkResponse?.statusCode == 500) {
                            viewState.showError("Проблемы с сервером, попробуйте позже")
                        } else {
                            val errorJson = JSONObject(String(it.networkResponse.data))
                            val reason = (errorJson["description"] as String)
                            if (errorJson.has("parameters")) {
                                val cloneId =
                                    ((errorJson["parameters"] as JSONObject)["clone_id"] as String)
                                viewState.onChoiceAlreadyExist(cloneId)
                            } else {
                                viewState.showError(reason)
                            }
                        }
                    })
            )
        }
    }

    fun addFavoriteQuestion(id: String) {
        requestQueue.add(
            addFavoriteRequest(
                app.authToken,
                id,
                { },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    private fun isValidChoice(firstText: String, lastText: String): Boolean {
        if ((firstText.length < 4) or (lastText.length < 4)) {
            viewState.showError(app.getString(R.string.new_choice_validation_error))
            return false
        }
        return true
    }
}
