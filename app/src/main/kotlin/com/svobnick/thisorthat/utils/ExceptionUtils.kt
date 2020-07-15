package com.svobnick.thisorthat.utils

import android.util.Log
import com.android.volley.VolleyError
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.reflect.KFunction1

object ExceptionUtils {
    private val TAG = this::class.java.name

    fun handleApiErrorResponse(
        it: VolleyError,
        showError: KFunction1<@ParameterName(name = "errorMsg") String, Unit>
    ) {
        if (it.networkResponse?.statusCode == 500) {
            showError.call("Проблемы с сервером, попробуйте позже")
        } else {
            if (it.networkResponse != null) {
                if (it.networkResponse.data != null && it.networkResponse.data.isNotEmpty()) {
                    val errorJson = JSONObject(String(it.networkResponse.data))
                    val reason = (errorJson["description"] as String)
                    showError.call(reason)
                } else {
                    val writer = PrintWriter(StringWriter())
                    RuntimeException().printStackTrace(writer)
                    Log.e(TAG, "Bad network response: ${it.networkResponse}. Stacktrace: \n " + writer.toString())
                }
            } else {
                showError.call("Проблемы с сетью, попробуйте позже")
            }
        }
    }
}