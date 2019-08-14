package com.svobnick.thisorthat.app

import androidx.room.Room
import com.android.volley.toolbox.Volley
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule constructor(thisOrThatApp: ThisOrThatApp) {

    lateinit var thisOrThatApp: ThisOrThatApp

    private var database =
        Room.databaseBuilder(thisOrThatApp.applicationContext, ApplicationDatabase::class.java, "database")
            .build()

    private var requestQueue = Volley.newRequestQueue(thisOrThatApp.applicationContext)

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
    fun getHttpClient() = requestQueue
}