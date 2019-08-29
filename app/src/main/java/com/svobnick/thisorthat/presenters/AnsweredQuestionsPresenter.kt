package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.view.AnsweredQuestionsView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@InjectViewState
class AnsweredQuestionsPresenter(
    private val questionDao: QuestionDao,
    private val requestQueue: RequestQueue
) : MvpPresenter<AnsweredQuestionsView>() {
    private val TAG = this::class.java.name

    fun getAnsweredQuestions() {
        questionDao.getAnsweredQuestions()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.setAnsweredQuestions(it)
                Log.i(TAG, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }
}