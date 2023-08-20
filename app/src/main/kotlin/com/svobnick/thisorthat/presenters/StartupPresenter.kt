package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.google.firebase.installations.FirebaseInstallations
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.registrationRequest
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.StartupView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.InjectViewState
import moxy.MvpPresenter
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
        FirebaseInstallations.getInstance().id.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.w(TAG, "Fetching firebase installation id failed", it.exception)
                return@addOnCompleteListener
            }

            val token = it.result
            Log.i(this::javaClass.name, "Firebase installation id: $token")

            Single.fromCallable {
                Thread.sleep(TWO_SECONDS) // two second for show startup screen
                val tokenFile = File(app.filesDir, "authToken")
                if (tokenFile.exists()) {
                    readToken(tokenFile)
                } else {
                    signUp(token, tokenFile)
                }
            }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    private fun readToken(tokenFile: File) {
        val authToken = tokenFile.readText()
        app.authToken = authToken
        Log.i(TAG, "Successfully read authToken: ${authToken}")
        viewState.onStartupEnd()
    }

    private fun signUp(instanceId: String, tokenFile: File) {
        requestQueue.add(
            registrationRequest(
                instanceId,
                {
                    val jsonResponse = JSONObject(it)
                    val result = jsonResponse["result"] as JSONObject
                    val authToken = result["token"] as String
                    tokenFile.writeText(authToken)
                    Log.i(TAG,"Successfully registration for instanceId: ${instanceId}, receive authToken: $authToken")
                    app.authToken = authToken
                    viewState.onStartupEnd()
                },
                {
                    Log.e(TAG,"Failed registration for instanceId: ${instanceId}")
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }
}