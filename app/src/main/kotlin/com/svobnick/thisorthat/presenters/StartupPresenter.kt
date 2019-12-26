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


@InjectViewState
class StartupPresenter(
    val application: ThisOrThatApp,
    val requestQueue: RequestQueue
) : MvpPresenter<StartupView>() {
    private val TAG = this::class.java.name

    fun checkRegistration() {
        val instanceId = FirebaseInstanceId.getInstance().id
        Log.i(this::javaClass.name, "Firebase instance id: $instanceId")

        val tokenFile = File(application.filesDir, "authToken")
        if (tokenFile.exists()) {
            val authToken = tokenFile.readText()
            application.authToken = authToken
            Log.i(TAG, "Read authToken $authToken from file")
        } else {
            signUp(instanceId, tokenFile)
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
        application.authToken = authToken
    }
}