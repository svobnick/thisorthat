package com.svobnick.thisorthat.service

import com.android.volley.Response
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
                result["views[${it.id}]"] = it.choice
            }
            return result
        }
    }

/**
 * https://docs.thisorthat.ru/#sendreport
 */
fun sendReportRequest(
    authToken: String,
    itemId: String,
    reason: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}sendReport",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("item_id", itemId),
                Pair("reason", reason)
            )
        }
    }

/**
 * https://docs.thisorthat.ru/#getfavorite
 */
fun getFavoriteRequest(
    json: JSONObject,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}getFavorite",
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
 * https://docs.thisorthat.ru/#addfavorite
 */
fun addFavoriteRequest(
    authToken: String,
    itemId: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}addFavorite",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("item_id", itemId)
            )
        }
    }

/**
 * https://docs.thisorthat.ru/#deletefavorite
 */
fun deleteFavoriteRequest(
    authToken: String,
    itemId: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}deleteFavorite",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("item_id", itemId)
            )
        }
    }


/**
 * https://docs.thisorthat.ru/#getcomments
 */
fun getCommentsRequest(
    authToken: String,
    itemId: String,
    limit: String,
    offset: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}getComments",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("item_id", itemId),
                Pair("limit", limit),
                Pair("offset", offset)
            )
        }
    }



/**
 * https://docs.thisorthat.ru/#addcomment
 */
fun addCommentRequest(
    authToken: String,
    itemId: String,
    text: String,
    parent: String,
    responseListener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) =
    object : StringRequest(
        Method.POST,
        "${apiAddress}addComment",
        responseListener,
        errorListener
    ) {
        override fun getParams(): MutableMap<String, String> {
            return mutableMapOf(
                Pair("token", authToken),
                Pair("item_id", itemId),
                Pair("message", text),
                Pair("parent", parent)
            )
        }
    }

