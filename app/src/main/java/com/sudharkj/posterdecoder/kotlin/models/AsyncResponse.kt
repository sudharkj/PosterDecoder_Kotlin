package com.sudharkj.posterdecoder.kotlin.models

interface AsyncResponse<Response> {
    fun processFinish(response: Response)
}