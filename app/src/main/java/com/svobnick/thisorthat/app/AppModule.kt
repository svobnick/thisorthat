package com.svobnick.thisorthat.app

import androidx.room.Room
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
            // todo requests must be done with asyncTasks, not in main thread
            .allowMainThreadQueries()
            .build()

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
}