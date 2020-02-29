package com.svobnick.thisorthat.app

import com.android.volley.RequestQueue
import com.svobnick.thisorthat.activities.*
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface InjectorComponent {

    fun inject(application: ThisOrThatApp)
    fun inject(startupActivity: StartupActivity)
    fun inject(chooseActivity: ChoiceActivity)
    fun inject(newQuestionActivity: NewQuestionActivity)
    fun inject(commentsActivity: CommentsActivity)
    fun inject(profileActivity: ProfileActivity)

    fun application(): ThisOrThatApp
    fun database(): ApplicationDatabase
    fun questionsDao(): QuestionDao
    fun claimsDao(): ClaimDao
    fun answersDao(): AnswerDao
    fun httpClient(): RequestQueue

}