package com.haneef.mindtrek.util

import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Timeout
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackgroundWorker {

    private val gson = Gson()

    fun <T> fetchData(url: String, method: String, payload: T? = null,token: String? = null, callback: ApiCallback) {
        val requestBuilder = Request.Builder().url(url)
        val client = OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build()
        when (method) {
            "GET" -> {
                requestBuilder
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Bearer $token")
                    .get()
                Log.d("FETCHDATA", "GET "+url)
            }
            "POST" -> {
                val jsonPayload = payload?.let { gson.toJson(it) }
                val body = jsonPayload?.toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Bearer $token")
                    .post(body!!)
            }
            else -> throw IllegalArgumentException("Unsupported method: $method")
        }

        val request = requestBuilder.build()

        client.newCall(request)
            .enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("FETCHDATA", "RESposnse "+responseBody)
                callback.onSuccess(responseBody)
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e.message ?: "Unknown error")
                Log.d("FETCHDATA", "ERROR "+e.message)
            }
        })
    }
}
