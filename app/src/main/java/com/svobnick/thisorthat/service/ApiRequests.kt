package com.svobnick.thisorthat.service

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import java.io.File

fun questionsRequest(responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) =
    JsonObjectRequest("http://dev.thisorthat.ru/api/items/get/20", null, responseListener, errorListener)

fun registrationRequest(tokenFile: File) =
    JsonObjectRequest(
        "http://dev.thisorthat.ru/api/users/add/",
        JSONObject()
            .put("client", "android")
            .put("unique", FirebaseInstanceId.getInstance().id),
        Response.Listener { response ->
            val token = response.get("token") as String
            println("Received token: $token")
            tokenFile.writeText(token)
        },
        Response.ErrorListener {
            System.err.println(it.message)
            it.printStackTrace()
        })