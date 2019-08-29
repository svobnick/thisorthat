package com.svobnick.thisorthat.presenters

import android.annotation.SuppressLint
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Answer
import com.svobnick.thisorthat.model.Claim
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.getNextQuestions
import com.svobnick.thisorthat.service.sendAnswersRequest
import com.svobnick.thisorthat.service.sendReportRequest
import com.svobnick.thisorthat.view.ChoiceView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@InjectViewState
class ChoicePresenter(
    private val application: ThisOrThatApp,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val claimDao: ClaimDao,
    private val requestQueue: RequestQueue
) : MvpPresenter<ChoiceView>() {
    private val TAG = this::class.java.name

    var currentQuestion: Question = Question.empty()
    var currentQuestionQueue: Queue<Question> = LinkedList()

    init {
        currentQuestion
        getUnansweredQuestions()
        if (!currentQuestionQueue.isEmpty()) {
            setNextQuestion()
        } else {
            getNewQuestions()
        }
    }

    private fun getNewQuestions() {
        requestQueue.add(
            getNextQuestions(
                application.authToken!!,
                Response.Listener { response ->
                    val questions2save = ArrayList<Question>()
                    val json = JSONObject(response)
                    val items = (json["result"] as JSONObject)["items"] as JSONArray
                    for (i in 0 until items.length()) {
                        val json = items.get(i) as JSONObject
                        questions2save.add(
                            Question(
                                (json["item_id"] as String).toLong(),
                                json["first_text"] as String,
                                json["last_text"] as String,
                                json["first_vote"] as Int,
                                json["last_vote"] as Int,
                                null
                            )
                        )
                    }
                    Single.fromCallable { questionDao.insertAll(questions2save) }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.i(TAG, "Successfully saved ${questions2save.size} new questions")
                        }, {
                            viewState.showError(it.localizedMessage)
                        })
                    getUnansweredQuestions()
                    setNextQuestion()
                },
                Response.ErrorListener {
                    Log.e(TAG, JSONObject(String(it.networkResponse.data)).toString())
                    it.printStackTrace()
                })
        )
    }

    @SuppressLint("CheckResult")
    fun saveChoice(choice: String) {
        currentQuestion.userChoice = choice.toInt() == currentQuestion.firstRate
        Single.fromCallable { questionDao.saveUserChoice(currentQuestion) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        // todo this thing needs refactoring
        val answer = if (choice.toInt() == currentQuestion.firstRate) "first" else "last"
        Single.fromCallable { answerDao.saveAnswer(Answer(currentQuestion.id, answer)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "Answer saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        val disposable = answerDao.getAnswers()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.size >= 10) {
                    Log.i(TAG, "Answers size is ${it.size}, try to send it to server")
                    val value = JSONObject();
                    it.forEach { answer -> value.put(answer.id.toString(), answer.userChoice) }
                    requestQueue.add(sendAnswersRequest(
                        application.authToken!!,
                        it,
                        Response.Listener {
                            Log.i(TAG, "Answers successfully was sent to server!")
                            Single.fromCallable {
                                answerDao.clear()
                            }
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(Schedulers.newThread())
                                .subscribe({
                                    Log.i(TAG, "Answers table successfully cleared!")
                                }, {
                                    Log.e(TAG, "Failed to clear answers table: ${it.localizedMessage}")
                                })
                        },
                        Response.ErrorListener {
                            Log.e(TAG, "Sending answers to server failed cause: ${it.localizedMessage}")
                            Log.e(TAG, "Server response data: ${JSONObject(String(it.networkResponse.data))}")
                        }
                    ))
                }
            }, {
                viewState.showError(it.localizedMessage)
            })
    }

    fun reportQuestion(reportReason: String) {
        val claim = Claim(currentQuestion.id, reportReason)
        var disposable = Single.fromCallable { claimDao.saveClaim(claim) }
            .subscribeOn(Schedulers.newThread())
        requestQueue.add(
            sendReportRequest(application.authToken!!,
                currentQuestion.id,
                reportReason,
                Response.Listener { response ->
                    Log.i(TAG, response.toString())
                },
                Response.ErrorListener {
                    Log.e(TAG, String(it.networkResponse.data))
                })
        )
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
                Log.i(TAG, "Receive ${it.size} unanswered questions")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }
}