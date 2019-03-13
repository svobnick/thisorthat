package com.svobnick.thisorthat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.questionsRequest
import com.svobnick.thisorthat.service.registrationRequest
import java.io.File
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var questionDao: QuestionDao
    @Inject lateinit var requestQueue: RequestQueue
    var currentQuestion: Question? = null
    var currentQuestionPool: Queue<Question>? = null
    lateinit var token: String

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as ThisOrThatApp).appComponent.inject(this)

        val tokenFile = File(applicationContext.filesDir, "token")
        if (tokenFile.exists()) {
            token = tokenFile.readText()
            println("Read token $token from file")
        } else {
            // todo when request will be done – we need to store global token
            requestQueue.add(registrationRequest(tokenFile))
        }

        getUnansweredQuestions()
        if (currentQuestionPool != null && !currentQuestionPool?.isEmpty()!!) {
            setNextQuestionToView()
        } else {
            getNewQuestions()
        }
    }

    val setNextQuestionToView = {
        val question = currentQuestionPool!!.poll()
        if (question != null) {
            val thisText = findViewById<TextView>(R.id.thisText)
            val thatText = findViewById<TextView>(R.id.thatText)
            thisText.text = question.thisText
            thatText.text = question.thatText
            currentQuestion = question
        } else {
            getNewQuestions()
        }
    }

    val getUnansweredQuestions = {
        currentQuestionPool = LinkedList(questionDao.getUnansweredQuestions())
    }


    private fun getNewQuestions() {
        requestQueue.add(questionsRequest(questionDao, getUnansweredQuestions, setNextQuestionToView))
    }

    fun onClickListener(view: View) {
        val clickedText = findViewById<TextView>(view.id)
        currentQuestion!!.userChoice = clickedText.text == currentQuestion!!.thisText
        questionDao.saveUserChoice(currentQuestion!!)
        setNextQuestionToView()
    }

    fun onMenuButtonClick(view: View) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}
