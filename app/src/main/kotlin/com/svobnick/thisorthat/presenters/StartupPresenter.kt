package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.firebase.iid.FirebaseInstanceId
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.service.registrationRequest
import com.svobnick.thisorthat.view.StartupView
import org.json.JSONObject
import java.io.File
import javax.inject.Inject


@InjectViewState
class StartupPresenter(val app: ThisOrThatApp) : MvpPresenter<StartupView>() {
    private val ONE_SECOND: Long = 1000
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

        val tokenFile = File(app.filesDir, "authToken")
        if (tokenFile.exists()) {
            val authToken = tokenFile.readText()
            app.authToken = authToken
            Log.i(TAG, "Read authToken $authToken from file")
            Thread.sleep(ONE_SECOND)
        } else {
            signUp(instanceId, tokenFile)
            Thread.sleep(ONE_SECOND)
        }
    }

    private fun signUp(instanceId: String, tokenFile: File) {
        val future = RequestFuture.newFuture<String>()
        val request = registrationRequest(
            instanceId,
            future,
            Response.ErrorListener {
                val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                Log.e(TAG, errorMsg)
                viewState.showError(errorMsg)
            })
        requestQueue.add(request)
        val response = future.get()

        handleSuccessResponse(response, tokenFile)
    }

    private fun handleSuccessResponse(response: String, tokenFile: File) {
        Log.i(this::javaClass.name, "Successful registration!")
        val jsonResponse = JSONObject(response)
        val result = jsonResponse["result"] as JSONObject
        val authToken = result["token"] as String
        tokenFile.writeText(authToken)
        app.authToken = authToken
    }
}