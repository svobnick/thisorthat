package com.svobnick.thisorthat.presenters

import com.android.volley.RequestQueue
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.questionsRequest
import com.svobnick.thisorthat.view.ChoiceView
import java.util.*
import com.android.volley.Response
import org.json.JSONObject


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
                    response.keys().forEach { key ->
                        val question = response.get(key) as JSONObject
                        questionDao.insertAll(
                            Question(
                                key.toLong(),
                                question.get("left_text").toString(),
                                question.get("right_text").toString(),
                                null
                            )
                        )
                    }
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
        questionDao.saveUserChoice(currentQuestion!!)
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
        currentQuestionQueue.addAll(questionDao.getUnansweredQuestions())
    }
}