package com.svobnick.thisorthat.app

import androidx.room.Room
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule constructor(thisOrThatApp: ThisOrThatApp) {

    lateinit var thisOrThatApp: ThisOrThatApp

    private var database: ApplicationDatabase =
        Room.databaseBuilder(thisOrThatApp.applicationContext, ApplicationDatabase::class.java, "database")
            .build()

    private var requestQueue: RequestQueue =
        Volley.newRequestQueue(thisOrThatApp.applicationContext)


    @Provides
    @Singleton
    fun getApplication(): ThisOrThatApp {
        return thisOrThatApp
    }

    @Provides
    @Singleton
    fun getDatabase(): ApplicationDatabase {
        return database
    }

    @Provides
    @Singleton
    fun getQuestionsDao(): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun getAnswerDao(): AnswerDao {
        return database.answerDao()
    }

    @Provides
    @Singleton
    fun getClaimDao(): ClaimDao {
        return database.claimDao()
    }

    @Provides
    @Singleton
    fun getHttpClient(): RequestQueue {
        return requestQueue
    }
}