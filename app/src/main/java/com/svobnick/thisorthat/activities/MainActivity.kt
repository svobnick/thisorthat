package com.svobnick.thisorthat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.HttpClient
import com.svobnick.thisorthat.service.questionsRequest
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var questionDao: QuestionDao
    var currentQuestion: Question? = null
    var currentQuestionPool: Queue<Question>? = null

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as ThisOrThatApp).appComponent.inject(this)

        getUnansweredQuestions()
        if (currentQuestionPool != null && !currentQuestionPool?.isEmpty()!!) {
            setNextQuestionToView()
        } else {
            getNewQuestions()
        }
    }

    val setNextQuestionToView = {
        val question = currentQuestionPool!!.poll()
        val thisText = findViewById<TextView>(R.id.thisText)
        val thatText = findViewById<TextView>(R.id.thatText)
        thisText.text = question.thisText
        thatText.text = question.thatText
        currentQuestion = question
    }

    val getUnansweredQuestions = {
        currentQuestionPool = LinkedList(questionDao.getUnansweredQuestions())
    }


    private fun getNewQuestions() {
        HttpClient.getInstance(this.applicationContext)
            .addToRequestQueue(questionsRequest(questionDao, getUnansweredQuestions, setNextQuestionToView))
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
