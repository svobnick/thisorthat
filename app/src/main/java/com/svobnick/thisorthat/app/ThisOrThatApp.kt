package com.svobnick.thisorthat.app

import android.app.Application
import android.util.Base64
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.google.firebase.iid.FirebaseInstanceId
import com.svobnick.thisorthat.service.registrationRequest
import java.io.File
import javax.inject.Inject

class ThisOrThatApp : Application() {

    lateinit var injector: InjectorComponent
    private var authToken: String? = null

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()

        injector = DaggerInjectorComponent.builder()
            .appModule(AppModule(this))
            .build()

        injector.inject(this)

        val instanceId = FirebaseInstanceId.getInstance().id
        Log.i(this::javaClass.name, "Firebase instance id: $instanceId")

        val tokenFile = File(applicationContext.filesDir, "authToken")
        if (tokenFile.exists()) {
            authToken = tokenFile.readText()
            Log.i(this::class.java.name, "Read authToken $authToken from file")
        } else {
            signUp(instanceId, tokenFile)
        }
    }

    private fun signUp(instanceId: String, tokenFile: File) {
        val registrationRequest = registrationRequest(
            instanceId,
            Response.Listener { response ->
                val user = response.get("user") as String
                val token = response.get("token") as String
                Log.i(this::class.java.name, "Receive answer: user $user with authToken $token")
                authToken = Base64.encodeToString("$user:$token".toByteArray(), Base64.DEFAULT)
                tokenFile.writeText(authToken!!)
            },
            Response.ErrorListener {
                Log.e(this::class.java.name, it.message)
                it.printStackTrace()
            })
        requestQueue.add(registrationRequest)
    }

    fun authToken(): String? {
        return authToken
    }
}