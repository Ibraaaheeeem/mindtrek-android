package com.haneef.mindtrek.util

interface ApiCallback {
    fun onSuccess(response: String)
    fun onError(error: String)
}