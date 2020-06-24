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
import com.svobnick.thisorthat.service.*
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.ChoiceView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

@InjectViewState
class ChoicePresenter(private val app: ThisOrThatApp) : MvpPresenter<ChoiceView>() {
    private val TAG = this::class.java.name

    @Inject
    lateinit var questionDao: QuestionDao

    @Inject
    lateinit var answerDao: AnswerDao

    @Inject
    lateinit var claimDao: ClaimDao

    @Inject
    lateinit var requestQueue: RequestQueue

    private var questionsQueue: Queue<Question> = LinkedList()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    private fun getNewQuestions() {
        requestQueue.add(
            getNextQuestions(
                app.authToken,
                Response.Listener {
                    val questions2save = ArrayList<Question>()
                    val json = JSONObject(it)
                    val items = (json["result"] as JSONObject)["items"] as JSONArray
                    for (i in 0 until items.length()) {
                        val item = items.get(i) as JSONObject
                        questions2save.add(
                            Question(
                                (item["item_id"] as String).toLong(),
                                item["first_text"] as String,
                                item["last_text"] as String,
                                item["first_vote"] as Int,
                                item["last_vote"] as Int,
                                item["status"] as String,
                                Question.Choices.NOT_ANSWERED
                            )
                        )
                    }
                    saveNewQuestions(questions2save)
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    private fun saveNewQuestions(questions2save: ArrayList<Question>) {
        val ids = questions2save.map { it.id }
        questionDao.getQuestionsByIds(ids)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ oldQuestions ->
                val oldIds = oldQuestions.map { it.id }.toSet()
                val newQuestions = questions2save.filter { !oldIds.contains(it.id) }
                Log.i(TAG, "Save ${newQuestions.size} new questions to database")
                Single.fromCallable { questionDao.insertAll(newQuestions) }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        fillUnansweredQuestionsQueue()
                    }, {
                        viewState.showError(it.localizedMessage)
                    })
            }, {
                viewState.showError(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    fun saveChoice(choice: Question) {
        Single.fromCallable { questionDao.saveUserChoice(choice) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG, "Choice saved successfully")
            }, {
                viewState.showError(it.localizedMessage)
            })

        Single.fromCallable { answerDao.saveAnswer(Answer(choice.id, choice.choice)) }
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
                    sendAnswers(it)
                }
            }, {
                viewState.showError(it.localizedMessage)
            })
    }

    private fun sendAnswers(it: List<Answer>) {
        val ids = it.map { it.id }
        requestQueue.add(sendAnswersRequest(
            app.authToken,
            it,
            Response.Listener {
                Log.i(TAG, "Answers successfully was sent to server!")
                Single.fromCallable {
                    answerDao.clear()
                    questionDao.deleteByIds(ids)
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
                ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
            }
        ))
    }

    fun reportQuestion(question: Question, reportReason: String) {
        val claim = Claim(question.id, reportReason)
        Single.fromCallable { claimDao.saveClaim(claim) }
            .subscribeOn(Schedulers.newThread())
        requestQueue.add(
            sendReportRequest(
                app.authToken,
                question.id.toString(),
                reportReason,
                Response.Listener {
                    Log.i(TAG, "Question successfully reported")
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
        question.choice = Question.Choices.SKIP
        saveChoice(question)
        setNextQuestion()
    }

    fun addFavoriteQuestion(id: String) {
        requestQueue.add(
            addFavoriteRequest(
                app.authToken,
                id,
                Response.Listener {
                    Log.i(TAG, it.toString())
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    fun deleteFavoriteQuestion(id: String) {
        requestQueue.add(
            deleteFavoriteRequest(
                app.authToken,
                id,
                Response.Listener {
                    Log.i(TAG, it.toString())
                },
                Response.ErrorListener {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    fun setNextQuestion() {
        val question = questionsQueue.poll()
        if (question != null) {
            viewState.setNewQuestion(question)
        } else {
            getNewQuestions()
        }
    }

    private fun fillUnansweredQuestionsQueue() {
        questionDao
            .getUnansweredQuestions()
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe({
                questionsQueue.clear() // to avoid inconsistencies (duplicates in queue)
                questionsQueue.addAll(it)
                setNextQuestion()
                Log.i(TAG, "Add ${it.size} unanswered questions to queue")
            }, {
                viewState.showError(it.localizedMessage)
            })
    }
}