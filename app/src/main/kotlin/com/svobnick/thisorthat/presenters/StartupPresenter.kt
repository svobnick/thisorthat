package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.firebase.iid.FirebaseInstanceId
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.registrationRequest
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.StartupView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.File
import javax.inject.Inject


@InjectViewState
class StartupPresenter(val app: ThisOrThatApp) : MvpPresenter<StartupView>() {
    private val TWO_SECONDS: Long = 2000
    private val TAG = this::class.java.name

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    fun checkRegistration() {
        val instanceId = FirebaseInstanceId.getInstance().id
        Log.i(this::javaClass.name, "Firebase instance id: $instanceId")

        Single.fromCallable {
            Thread.sleep(TWO_SECONDS) // two second for show startup screen
            val tokenFile = File(app.filesDir, "authToken")
            if (tokenFile.exists()) {
                readToken(tokenFile)
            } else {
                signUp(instanceId, tokenFile)
            }
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun readToken(tokenFile: File) {
        val authToken = tokenFile.readText()
        app.authToken = authToken
        viewState.onStartupEnd()
    }

    private fun signUp(instanceId: String, tokenFile: File) {
        requestQueue.add(
            registrationRequest(
                instanceId,
                Response.Listener {
                    val jsonResponse = JSONObject(it)
                    val result = jsonResponse["result"] as JSONObject
                    val authToken = result["token"] as String
                    tokenFile.writeText(authToken)
                    app.authToken = authToken
                    viewState.onStartupEnd()
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }
}