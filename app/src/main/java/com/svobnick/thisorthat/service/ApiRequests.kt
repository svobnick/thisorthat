package com.svobnick.thisorthat.service

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.svobnick.thisorthat.model.Answer
import org.json.JSONObject

const val apiAddress = "https://api.thisorthat.ru/"

/**
 * https://docs.thisorthat.ru/#register
 */
fun registrationRequest(
    instanceId: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}register",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("client", "android_v2"),
                Pair("uniqid", instanceId)
            )
        }
    }

/**
 * https://docs.thisorthat.ru/#getitems
 */
fun getNextQuestions(
    authToken: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}getItems",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("status", "approved")
            )
        }
    }

/**
 * https://docs.thisorthat.ru/#additem
 */
fun sendNewQuestion(
    json: JSONObject,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}addItem",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", json["token"] as String),
                Pair("first_text", json["first_text"] as String),
                Pair("last_text", json["last_text"] as String)
            )
        }
    }

/**
 * https://docs.thisorthat.ru/#getmyitems
 */
fun getMyQuestions(
    json: JSONObject,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}getMyItems",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", json["token"] as String),
                Pair("limit", json["limit"] as String),
                Pair("offset", json["offset"] as String)
            )
        }
    }


/**
 * https://docs.thisorthat.ru/#setviewed
 */
fun sendAnswersRequest(
    authToken: String,
    answers: Collection<Answer>,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}setViewed",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            val result = mutableMapOf(Pair("token", authToken))
            answers.forEach {
                result["views[${it.id}]"] = it.userChoice
            }
            return result
        }
    }

private fun buildJsonAnswersRequest(answers: Collection<Answer>): JSONObject {
    val views = JSONObject()
    answers.forEach { views.put(it.id.toString(), it.userChoice) }
    return JSONObject().put("views", views)
}

/**
 * https://github.com/antonlukin/thisorthat-api/wiki/API:abuse#post-abuseadd
 */
fun sendClaimsRequest(
    authToken: String, json: JSONObject,
    responseListener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener
) =
    object : JsonObjectRequest(
        Method.POST,
        "${apiAddress}abuse/add/",
        json,
        responseListener,
        errorListener
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Basic $authToken"
            return headers
        }
    }