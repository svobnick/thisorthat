package com.svobnick.thisorthat.app

import androidx.room.Room
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.service.ApplicationDatabase
import com.svobnick.thisorthat.service.BottomMenuState
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule constructor(thisOrThatApp: ThisOrThatApp) {

    private lateinit var thisOrThatApp: ThisOrThatApp

    private var database =
        Room.databaseBuilder(thisOrThatApp.applicationContext, ApplicationDatabase::class.java, "thisorthat-db-2")
            .build()

    private val picasso = Picasso.get()

    private var requestQueue = Volley.newRequestQueue(thisOrThatApp.applicationContext)

    private val menuInteractor = BottomMenuState()

    private val firebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun getApplication() = thisOrThatApp

    @Provides
    @Singleton
    fun getDatabase() = database

    @Provides
    @Singleton
    fun getQuestionsDao() = database.questionDao()

    @Provides
    @Singleton
    fun getAnswerDao() = database.answerDao()

    @Provides
    @Singleton
    fun getClaimDao() = database.claimDao()

    @Provides
    @Singleton
    fun getHttpClient(): RequestQueue = requestQueue

    @Provides
    @Singleton
    fun getPicasso(): Picasso = picasso

    @Provides
    @Singleton
    fun getMenuInteractor(): BottomMenuState = menuInteractor

    @Provides
    @Singleton
    fun getAnalytics(): FirebaseAnalytics = firebaseAnalytics
}