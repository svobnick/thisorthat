package com.svobnick.thisorthat.presenters

import android.util.Log
import com.android.volley.RequestQueue
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.questionsRequest
import com.svobnick.thisorthat.view.ChoiceView
import java.util.*
import com.android.volley.Response
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import kotlin.collections.ArrayList


@InjectViewState
class ChoicePresenter(
    private val questionDao: QuestionDao,
    private val requestQueue: RequestQueue
) : MvpPresenter<ChoiceView>() {

    var currentQuestion: Question? = null
    var currentQuestionQueue: Queue<Question> = LinkedList()

    init {
        getUnansweredQuestions()
        if (!currentQuestionQueue.isEmpty()) {
            setNextQuestion()
        } else {
            getNewQuestions()
        }
    }

    private fun getNewQuestions() {
        requestQueue.add(
            questionsRequest(
                Response.Listener { response ->
                    val questions2save = ArrayList<Question>()
                    response.keys().forEach { key ->
                        val question = response.get(key) as JSONObject
                        questions2save.add(
                            Question(
                                key.toLong(),
                                question.get("left_text").toString(),
                                question.get("right_text").toString(),
                                null
                            )
                        )
                    }
                    Single.fromCallable { questionDao.insertAll(questions2save) }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.i(this::class.java.name, "Successfully saved ${questions2save.size} new questions")
                        }, {
                            viewState.showError(it.localizedMessage)
                        })
                    getUnansweredQuestions()
                    setNextQuestion()
                },
                Response.ErrorListener {
                    System.err.println(it.message)
                    it.printStackTrace()
                })
        )
    }

    fun saveChoice(choice: CharSequence) {
        currentQuestion!!.userChoice = choice == currentQuestion!!.thisText
        Single.fromCallable { questionDao.saveUserChoice(currentQuestion!!) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(this::class.java.name, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }

    fun skipQuestion() {
        // todo save choice to dao
        setNextQuestion()
    }

    fun setNextQuestion() {
        val question = currentQuestionQueue.poll()
        if (question != null) {
            viewState.setNewQuestion(question)
            currentQuestion = question
        } else {
            getNewQuestions()
        }
    }

    fun getUnansweredQuestions() {
        questionDao
            .getUnansweredQuestions()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                currentQuestionQueue.addAll(it)
                Log.i(this::class.java.name, "Receive ${it.size} unanswered questions")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }
}