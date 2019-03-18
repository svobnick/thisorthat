package com.svobnick.thisorthat.service

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject

fun questionsRequest(authToken: String, responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) =
    object: JsonObjectRequest("http://dev.thisorthat.ru/api/items/get/20", null, responseListener, errorListener) {
        override fun getParams(): MutableMap<String, String> {
            val params = HashMap<String, String>()
            Log.i(this::class.java.name, "Add authToken $authToken to params")
            params["Authorization"] = "Basic $authToken"
            return params
        }
    }

fun registrationRequest(responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) =
    JsonObjectRequest(
        "http://dev.thisorthat.ru/api/users/add/",
        JSONObject()
            .put("client", "android")
            .put("unique", FirebaseInstanceId.getInstance().id),
        responseListener,
        errorListener)