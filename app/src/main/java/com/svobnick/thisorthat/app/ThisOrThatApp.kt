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
    private var authToken: String = ""

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()

        injector = DaggerInjectorComponent.builder()
            .appModule(AppModule(this))
            .build()

        injector.inject(this)

        // todo init latch (block ui-thread until token would be received)
        val instanceId = FirebaseInstanceId.getInstance().id
        Log.i(this::javaClass.name, "Firebase instance id: $instanceId")
        val tokenFile = File(applicationContext.filesDir, "authToken")
        if (tokenFile.exists()) {
            authToken = tokenFile.readText()
            Log.i(this::class.java.name, "Read authToken $authToken from file")
        } else {
            requestQueue.add(
                registrationRequest(
                    instanceId,
                    Response.Listener { response ->
                        val user = response.get("user") as String
                        val token = response.get("token") as String
                        Log.i(this::class.java.name, "Receive answer: user $user with authToken $token")
                        authToken = Base64.encodeToString("$user:$token".toByteArray(), Base64.DEFAULT)
                        tokenFile.writeText(authToken)
                    },
                    Response.ErrorListener {
                        Log.e(this::class.java.name, it.message)
                        it.printStackTrace()
                    })
            )
        }
    }

    fun authToken(): String {
        return authToken
    }
}