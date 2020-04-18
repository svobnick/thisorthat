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
import com.svobnick.thisorthat.service.addFavoriteRequest
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
import javax.inject.Inject

@InjectViewState
class ChoicePresenter(private val application: ThisOrThatApp) : MvpPresenter<ChoiceView>() {
    private val TAG = this::class.java.name

    @Inject
    lateinit var questionDao: QuestionDao

    @Inject
    lateinit var answerDao: AnswerDao

    @Inject
    lateinit var claimDao: ClaimDao

    @Inject
    lateinit var requestQueue: RequestQueue

    var currentQuestion: Question = Question.empty()
    var currentQuestionQueue: Queue<Question> = LinkedList()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        application.injector.inject(this)
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
                application.authToken,
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
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    viewState.showError(errorMsg)
                })
        )
    }

    @SuppressLint("CheckResult")
    fun saveChoice(choice: String): String {
        val resultChoice = when (choice) {
            Question.SKIPPED -> Question.SKIPPED
            currentQuestion.firstText -> Question.FIRST
            currentQuestion.lastText -> Question.LAST
            else -> throw IllegalArgumentException("Strange choice $choice")
        }
        currentQuestion.choice = resultChoice

        Single.fromCallable { questionDao.saveUserChoice(currentQuestion) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        Single.fromCallable { answerDao.saveAnswer(Answer(currentQuestion.id, resultChoice)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "Answer saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        answerDao.getAnswers()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ it ->
                if (it.size >= 10) {
                    Log.i(TAG, "Answers size is ${it.size}, try to send it to server")
                    val value = JSONObject()
                    it.forEach { answer -> value.put(answer.id.toString(), answer.choice) }
                    requestQueue.add(sendAnswersRequest(
                        application.authToken,
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
                                    Log.e(
                                        TAG,
                                        "Failed to clear answers table: ${it.localizedMessage}"
                                    )
                                    viewState.showError(it.localizedMessage)
                                })
                        },
                        Response.ErrorListener {
                            Log.e(
                                TAG,
                                "Sending answers to server failed cause: ${it.localizedMessage}"
                            )
                            val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                            Log.e(TAG, "Server response data: $errorMsg")
                            viewState.showError(errorMsg)
                        }
                    ))
                }
            }, {
                viewState.showError(it.localizedMessage)
            })

        return resultChoice
    }

    fun reportQuestion(reportReason: String) {
        val claim = Claim(currentQuestion.id, reportReason)
        Single.fromCallable { claimDao.saveClaim(claim) }
            .subscribeOn(Schedulers.newThread())
        requestQueue.add(
            sendReportRequest(
                application.authToken,
                currentQuestion.id,
                reportReason,
                Response.Listener { response ->
                    Log.i(TAG, response.toString())
                },
                Response.ErrorListener {
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    Log.e(TAG, errorMsg)
                    viewState.showError(errorMsg)
                })
        )
        saveChoice(Question.SKIPPED)
        setNextQuestion()
    }

    fun addFavoriteQuestion() {
        requestQueue.add(
            addFavoriteRequest(
                application.authToken,
                currentQuestion.id.toString(),
                Response.Listener { response ->
                    Log.i(TAG, response.toString())
                },
                Response.ErrorListener {
                    val errorMsg = JSONObject(String(it.networkResponse.data)).toString()
                    Log.e(TAG, errorMsg)
                    viewState.showError(errorMsg)
                })
        )
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

    private fun getUnansweredQuestions() {
        questionDao
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