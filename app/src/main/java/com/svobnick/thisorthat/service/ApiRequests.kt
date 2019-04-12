package com.svobnick.thisorthat.service

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.svobnick.thisorthat.model.Answer
import org.json.JSONObject

// todo get api address from properties during application build
const val apiAddress = "http://dev.thisorthat.ru/api/"

/**
 * https://github.com/antonlukin/thisorthat-api/wiki/API:items#get-itemsget
 */
fun questionsRequest(authToken: String?,
                     responseListener: Response.Listener<JSONObject>,
                     errorListener: Response.ErrorListener) =
    object: JsonObjectRequest(
        Method.GET,
        "${apiAddress}items/get/20",
        null,
        responseListener,
        errorListener) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            if (authToken != null) {
                headers["Authorization"] = "Basic $authToken"
            }
            return headers
        }
    }

/**
 * https://github.com/antonlukin/thisorthat-api/wiki/API:views#post-viewsadd
 */
fun sendAnswersRequest(authToken: String, answers: Collection<Answer>,
                       responseListener: Response.Listener<JSONObject>,
                       errorListener: Response.ErrorListener) =
    object: JsonObjectRequest(
        Method.POST,
        "${apiAddress}views/add/",
        buildJsonAnswersRequest(answers),
        responseListener,
        errorListener) {

        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Basic $authToken"
            return headers
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
fun sendClaimsRequest(authToken: String, json: JSONObject,
                      responseListener: Response.Listener<JSONObject>,
                      errorListener: Response.ErrorListener) =
    object: JsonObjectRequest(
        Method.POST,
        "${apiAddress}abuse/add/",
        json,
        responseListener,
        errorListener) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Basic $authToken"
            return headers
        }
    }

/**
 * https://github.com/antonlukin/thisorthat-api/wiki/API:users#post-usersadd
 */
fun registrationRequest(instanceId: String,
                        responseListener: Response.Listener<JSONObject>,
                        errorListener: Response.ErrorListener) =
    object: JsonObjectRequest(
        Method.POST,
        "${apiAddress}users/add/",
        JSONObject()
            .put("client", "android")
            .put("unique", instanceId),
        responseListener,
        errorListener) {

    }

/**
 * https://github.com/antonlukin/thisorthat-api/wiki/API:items#post-itemsadd
 */
fun sendNewQuestion(authToken: String, json: JSONObject,
                    responseListener: Response.Listener<JSONObject>,
                    errorListener: Response.ErrorListener) =
    object: JsonObjectRequest(
        Method.POST,
        "${apiAddress}items/add/",
        json,
        responseListener,
        errorListener) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Basic $authToken"
            return headers
        }
    }