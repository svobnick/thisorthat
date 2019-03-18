package com.svobnick.thisorthat.app

import android.app.Application
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.svobnick.thisorthat.service.registrationRequest
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class ThisOrThatApp : Application() {

    lateinit var injector: InjectorComponent
    private lateinit var authToken: String

    @Inject
    lateinit var requestQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()

        injector = DaggerInjectorComponent.builder()
            .appModule(AppModule(this))
            .build()

        injector.inject(this)

        val initLatch = CountDownLatch(1)
        val tokenFile = File(applicationContext.filesDir, "authToken")
        if (tokenFile.exists()) {
            authToken = tokenFile.readText()
            initLatch.countDown()
            Log.i(this::class.java.name, "Read authToken $authToken from file")
        } else {
            requestQueue.add(
                registrationRequest(
                    Response.Listener { response ->
                        val newToken = response.get("token") as String
                        Log.i(this::class.java.name, "Received authToken: $newToken")
                        tokenFile.writeText(newToken)
                        authToken = newToken
                        initLatch.countDown()
                    },
                    Response.ErrorListener {
                        Log.e(this::class.java.name, it.message)
                        it.printStackTrace()
                        initLatch.countDown()
                    })
            )
        }
        initLatch.await()
    }

    fun authToken(): String {
        return authToken
    }
}