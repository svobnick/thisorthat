package com.svobnick.thisorthat.presenters

import android.annotation.SuppressLint
import android.util.Log
import com.android.volley.RequestQueue
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Answer
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.*
import com.svobnick.thisorthat.utils.ExceptionUtils
import com.svobnick.thisorthat.view.ChoiceView
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

@InjectViewState
class ChoicePresenter(private val app: ThisOrThatApp) : MvpPresenter<ChoiceView>() {
    private val TAG = this::class.java.name

    @Inject
    lateinit var requestQueue: RequestQueue

    private var questionsQueue: TreeMap<Long, Question> = TreeMap()
    private var answersQueue: LinkedList<Answer> = LinkedList()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        app.injector.inject(this)
    }

    private fun getNewQuestions() {
        requestQueue.add(
            getNextQuestions(
                app.authToken,
                {
                    val questions2save = HashMap<Long, Question>()
                    val json = JSONObject(it)
                    val items = (json["result"] as JSONObject)["items"] as JSONArray
                    for (i in 0 until items.length()) {
                        val item = items.get(i) as JSONObject
                        val question = Question(
                            (item["item_id"] as String).toLong(),
                            item["first_text"] as String,
                            item["last_text"] as String,
                            item["first_vote"] as Int,
                            item["last_vote"] as Int,
                            item["status"] as String,
                            Question.Choices.NOT_ANSWERED
                        )
                        questions2save[question.id] = question
                    }
                    saveNewQuestions(questions2save)
                },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    private fun saveNewQuestions(questions2save: HashMap<Long, Question>) {
        for (entry in questions2save.entries) {
            if (!questionsQueue.containsKey(entry.key)) {
                questionsQueue[entry.key] = entry.value
            }
        }
        setNextQuestion()
    }

    @SuppressLint("CheckResult")
    fun saveChoice(choice: Question) {
        answersQueue.push(Answer(choice.id, choice.choice))

        if (answersQueue.size >= 10) {
            Log.i(TAG, "Answers queue size is ${answersQueue.size}, try to send it to server")
            val answers2send = ArrayList<Answer>(10)
            repeat(10) { answers2send.add(answersQueue.poll()!!) }
            sendAnswers(answers2send)
        }
    }

    private fun sendAnswers(it: Collection<Answer>) {
        requestQueue.add(sendAnswersRequest(
            app.authToken,
            it,
            {
                Log.i(TAG, "Answers successfully was sent to server!")
            },
            {
                ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
            }
        ))
    }

    fun reportQuestion(question: Question, reportReason: String) {
        requestQueue.add(
            sendReportRequest(
                app.authToken,
                question.id.toString(),
                reportReason,
                {
                    Log.i(TAG, "Question successfully reported")
                },
                {
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
                {
                    Log.i(TAG, it.toString())
                },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    fun deleteFavoriteQuestion(id: String) {
        requestQueue.add(
            deleteFavoriteRequest(
                app.authToken,
                id,
                {
                    Log.i(TAG, it.toString())
                },
                {
                    ExceptionUtils.handleApiErrorResponse(it, viewState::showError)
                })
        )
    }

    fun setNextQuestion() {
        val question = questionsQueue.pollFirstEntry()
        if (question != null) {
            viewState.setNewQuestion(question.value)
        } else {
            getNewQuestions()
        }
    }
}