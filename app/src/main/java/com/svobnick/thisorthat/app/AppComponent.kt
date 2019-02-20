package com.svobnick.thisorthat.app

import com.svobnick.thisorthat.activities.MainActivity
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun application(): ThisOrThatApp
    fun database(): ApplicationDatabase
    fun questionsDao(): QuestionDao

}