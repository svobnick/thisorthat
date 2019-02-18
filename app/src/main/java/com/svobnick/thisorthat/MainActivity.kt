package com.svobnick.thisorthat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.ApplicationDatabase
import com.svobnick.thisorthat.service.HttpClient
import com.svobnick.thisorthat.service.questionsRequest
import java.util.*

class MainActivity : AppCompatActivity() {

    private var database: ApplicationDatabase? = null
    private var currentQuestion: Question? = null
    private var currentQuestionPool: Queue<Question>? = null

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // todo must be standalone component with own lifecycle (remove from activity)
        database = Room.databaseBuilder(this, ApplicationDatabase::class.java, "database")
            // todo requests must be done with asyncTasks, not in main thread
            .allowMainThreadQueries()
            .build()

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
        currentQuestionPool = LinkedList(database?.questionDao()?.getUnansweredQuestions())
    }


    private fun getNewQuestions() {
        HttpClient.getInstance(this.applicationContext)
            .addToRequestQueue(questionsRequest(database!!, getUnansweredQuestions, setNextQuestionToView))
    }

    fun onClickListener(view: View) {
        val clickedText = findViewById<TextView>(view.id)
        currentQuestion!!.userChoice = clickedText.text == currentQuestion!!.thisText
        database?.questionDao()?.saveUserChoice(currentQuestion!!)
        setNextQuestionToView()
    }
}
