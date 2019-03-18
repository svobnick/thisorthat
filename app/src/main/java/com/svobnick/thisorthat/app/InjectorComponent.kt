package com.svobnick.thisorthat.app

import com.android.volley.RequestQueue
import com.svobnick.thisorthat.activities.AnsweredQuestionsActivity
import com.svobnick.thisorthat.activities.ChoiceActivity
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface InjectorComponent {

    fun inject(chooseActivity: ChoiceActivity)

    fun inject(answeredQuestionsActivity: AnsweredQuestionsActivity)

    fun application(): ThisOrThatApp
    fun database(): ApplicationDatabase
    fun questionsDao(): QuestionDao
    fun httpClient(): RequestQueue

}