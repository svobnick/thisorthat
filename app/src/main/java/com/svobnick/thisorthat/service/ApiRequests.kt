package com.svobnick.thisorthat.service

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question
import org.json.JSONObject
import java.util.*

fun questionsRequest(questionDao: QuestionDao, unansweredQuestions: () -> Unit, nextQuestionToView: () -> Unit) =
    JsonObjectRequest(
        "http://dev.thisorthat.ru/api/items/get/20", null,
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
            unansweredQuestions.invoke()
            nextQuestionToView.invoke()
        },
        Response.ErrorListener {
            System.err.println(it.message)
            it.printStackTrace()
        })

fun registrationRequest() = {
    var requestJson = JSONObject()
    requestJson.put("client", "android")
    // todo use firebase instance id for registration
    requestJson.put("unique", UUID.randomUUID().toString())
    JsonObjectRequest(
        "http://dev.thisorthat.ru/api/users/add/", requestJson,
        Response.Listener { response ->

        },
        Response.ErrorListener {
            System.err.println(it.message)
            it.printStackTrace()
        })
}
