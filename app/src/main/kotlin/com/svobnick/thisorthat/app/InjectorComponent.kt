package com.svobnick.thisorthat.app

import com.android.volley.RequestQueue
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.activities.MainScreenActivity
import com.svobnick.thisorthat.activities.StartupActivity
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.fragments.ChoiceFragment
import com.svobnick.thisorthat.fragments.NewChoiceFragment
import com.svobnick.thisorthat.fragments.ProfileFragment
import com.svobnick.thisorthat.presenters.*
import com.svobnick.thisorthat.service.ApplicationDatabase
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface InjectorComponent {

    fun inject(application: ThisOrThatApp)
    fun inject(activity: StartupActivity)
    fun inject(activity: MainScreenActivity)
    fun inject(activity: CommentsActivity)
    fun inject(activity: HistoryChoiceActivity)

    fun inject(fragment: ChoiceFragment)
    fun inject(fragment: NewChoiceFragment)
    fun inject(fragment: ProfileFragment)

    fun inject(presenter: StartupPresenter)
    fun inject(presenter: ChoicePresenter)
    fun inject(presenter: NewChoicePresenter)
    fun inject(presenter: ProfilePresenter)
    fun inject(presenter: MainScreenPresenter)
    fun inject(presenter: BottomMenuPresenter)
    fun inject(presenter: HistoryChoicePresenter)
    fun inject(presenter: CommentsPresenter)

    fun application(): ThisOrThatApp
    fun database(): ApplicationDatabase
    fun questionsDao(): QuestionDao
    fun claimsDao(): ClaimDao
    fun answersDao(): AnswerDao
    fun httpClient(): RequestQueue

}