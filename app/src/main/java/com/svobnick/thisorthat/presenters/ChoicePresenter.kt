package com.svobnick.thisorthat.presenters

import android.annotation.SuppressLint
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
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.model.Answer
import com.svobnick.thisorthat.model.Claim
import com.svobnick.thisorthat.model.ClaimReason
import com.svobnick.thisorthat.service.sendAnswersRequest
import com.svobnick.thisorthat.service.sendClaimsRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import kotlin.collections.ArrayList

@InjectViewState
class ChoicePresenter(
    private val application: ThisOrThatApp,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val claimDao: ClaimDao,
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
                application.authToken(),
                Response.Listener { response ->
                    val questions2save = ArrayList<Question>()
                    response.keys().forEach { key ->
                        val question = response.get(key) as JSONObject
                        questions2save.add(
                            Question(
                                key.toLong(),
                                question.get("left_text").toString(),
                                question.get("right_text").toString(),
                                question.get("left_vote").toString().toInt(),
                                question.get("right_vote").toString().toInt(),
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
                    Log.e(this::class.java.name, String(it.networkResponse.data))
                    it.printStackTrace()
                })
        )
    }

    @SuppressLint("CheckResult")
    fun saveChoice(choice: CharSequence) {
        currentQuestion!!.userChoice = choice == currentQuestion!!.firstText
        Single.fromCallable { questionDao.saveUserChoice(currentQuestion!!) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(this::class.java.name, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        val choice = if (choice == currentQuestion!!.firstText) "left" else "right"
        Single.fromCallable { answerDao.saveAnswer(Answer(currentQuestion!!.id, choice)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(this::class.java.name, "Answer saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        val disposable = answerDao.getAnswers()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.size >= 10 && application.authToken() != null) {
                    Log.i(this::class.java.name, "Answers size is ${it.size}, try to send it to server")
                    val value = JSONObject();
                    it.forEach { answer -> value.put(answer.id.toString(), answer.userChoice) }
                    requestQueue.add(sendAnswersRequest(
                        application.authToken()!!,
                        it,
                        Response.Listener {
                            Log.i(this::class.java.name, "Answers successfully was sent to server!")
                            Single.fromCallable {
                                answerDao.clear()
                            }
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(Schedulers.newThread())
                                .subscribe({
                                    Log.i(this::class.java.name, "Answers table successfully cleared!")
                                }, {
                                    Log.e(this::class.java.name, "Failed to clear answers table: ${it.localizedMessage}")
                                })
                        },
                        Response.ErrorListener { err ->
                            Log.e(
                                this::class.java.name,
                                "Sending answers to server failed cause: ${err.localizedMessage}"
                            )
                            Log.e(
                                this::class.java.name,
                                "Server response data: ${String(err.networkResponse.data)}"
                            )
                        }
                    ))
                }
            }, {
                viewState.showError(it.localizedMessage)
            })
    }

    fun claimQuestion(claimReason: String) {
        val claim = Claim(currentQuestion!!.id, claimReason)
        var disposable = Single.fromCallable { claimDao.saveClaim(claim) }
            .subscribeOn(Schedulers.newThread())
        if (application.authToken() != null) {
            val json = JSONObject().put("abuse", JSONObject().put(claim.id.toString(), claimReason))
            requestQueue.add(
                sendClaimsRequest(application.authToken()!!, json,
                    Response.Listener { response ->
                        Log.i(this::class.java.name, response.toString())
                    },
                    Response.ErrorListener {
                        Log.e(this::class.java.name, String(it.networkResponse.data))
                    })
            )
        }
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
        val disposable = questionDao
            .getUnansweredQuestions()
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe({
                currentQuestionQueue.addAll(it)
                Log.i(this::class.java.name, "Receive ${it.size} unanswered questions")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }
}