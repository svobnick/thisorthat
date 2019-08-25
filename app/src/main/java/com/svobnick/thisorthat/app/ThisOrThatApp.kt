package com.svobnick.thisorthat.app

import android.app.Application

class ThisOrThatApp : Application() {

    lateinit var injector: InjectorComponent
    internal var authToken: String? = null

    override fun onCreate() {
        super.onCreate()

        injector = DaggerInjectorComponent.builder()
            .appModule(AppModule(this))
            .build()

        injector.inject(this)
    }

    fun authToken(): String? {
        return authToken
    }
}