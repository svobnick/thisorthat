package com.svobnick.thisorthat.app

import androidx.room.Room
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.service.ApplicationDatabase
import com.svobnick.thisorthat.service.MenuInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule constructor(thisOrThatApp: ThisOrThatApp) {

    private lateinit var thisOrThatApp: ThisOrThatApp

    private var database =
        Room.databaseBuilder(thisOrThatApp.applicationContext, ApplicationDatabase::class.java, "thisorthat-db")
            .build()

    private val picasso = Picasso.get()

    private var requestQueue = Volley.newRequestQueue(thisOrThatApp.applicationContext)

    private val menuInteractor = MenuInteractor()


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
    fun getMenuInteractor(): MenuInteractor = menuInteractor
}