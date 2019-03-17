package com.svobnick.thisorthat.app

import android.app.Application

class ThisOrThatApp : Application() {

    lateinit var injector: InjectorComponent

    override fun onCreate() {
        super.onCreate()

        injector = DaggerInjectorComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}