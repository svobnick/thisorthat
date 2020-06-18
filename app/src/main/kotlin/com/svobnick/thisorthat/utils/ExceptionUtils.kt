package com.svobnick.thisorthat.utils

import com.android.volley.VolleyError
import org.json.JSONObject
import kotlin.reflect.KFunction1

object ExceptionUtils {

    fun handleApiErrorResponse(
        it: VolleyError,
        showError: KFunction1<@ParameterName(name = "errorMsg") String, Unit>
    ) {
        if (it.networkResponse?.statusCode == 500) {
            // todo use strings.xml
            showError.call("Проблемы с сервером, попробуйте позже")
        } else {
            val errorJson = JSONObject(String(it.networkResponse.data))
            val reason = (errorJson["description"] as String)
            showError.call(reason)
        }
    }
}