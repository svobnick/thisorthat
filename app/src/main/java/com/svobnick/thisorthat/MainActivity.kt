package com.svobnick.thisorthat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getQuestions()
    }

    val questionsRequest = JsonObjectRequest(
        Request.Method.GET, "https://thisorthat.ru/api/items/get/20", null,
        Response.Listener { response ->
            val thisText = findViewById<TextView>(R.id.thisText)
            val thatText = findViewById<TextView>(R.id.thatText)
            println("breakpoint")
            response.keys().forEach {key ->
                val question = response.get(key) as JSONObject
                thisText.text = question.get("left_text").toString()
                thatText.text = question.get("right_text").toString()
            }
        },
        Response.ErrorListener {
            println("breakpoint")
        })


    private fun getQuestions() {
        ApiCommands.getInstance(this.applicationContext).addToRequestQueue(questionsRequest)

    }

    fun onClickListener(view: View) {
        val clickedText = findViewById<TextView>(view.id)
        println(clickedText.text)
    }
}
