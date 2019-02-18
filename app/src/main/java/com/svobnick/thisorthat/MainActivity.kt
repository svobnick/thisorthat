package com.svobnick.thisorthat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.service.ApplicationDatabase
import com.svobnick.thisorthat.service.HttpClient
import org.json.JSONObject
import java.lang.Long.valueOf

class MainActivity : AppCompatActivity() {

    private var database: ApplicationDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = Room.databaseBuilder(this, ApplicationDatabase::class.java!!, "database").allowMainThreadQueries().build()
        getQuestions()
    }

    val questionsRequest = JsonObjectRequest(
        Request.Method.GET, "https://thisorthat.ru/api/items/get/20", null,
        Response.Listener { response ->
            val thisText = findViewById<TextView>(R.id.thisText)
            val thatText = findViewById<TextView>(R.id.thatText)
            println("breakpoint")
            response.keys().forEach { key ->
                val question = response.get(key) as JSONObject
                thisText.text = question.get("left_text").toString()
                thatText.text = question.get("right_text").toString()
                database?.questionDao()?.insertAll(
                    Question(
                        valueOf(key),
                        question.get("left_text").toString(),
                        question.get("right_text").toString()
                    )
                )
            }
        },
        Response.ErrorListener {
            println("breakpoint")
        })


    private fun getQuestions() {
        HttpClient.getInstance(this.applicationContext).addToRequestQueue(questionsRequest)

    }

    fun onClickListener(view: View) {
        val clickedText = findViewById<TextView>(view.id)
        println(clickedText.text)
        val all = database?.questionDao()?.getAll()
        println(all)
    }
}
