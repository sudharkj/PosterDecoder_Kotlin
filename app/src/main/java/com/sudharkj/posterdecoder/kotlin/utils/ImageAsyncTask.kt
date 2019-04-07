package com.sudharkj.posterdecoder.kotlin.utils

import android.os.AsyncTask
import com.sudharkj.posterdecoder.kotlin.models.AsyncObject
import com.sudharkj.posterdecoder.kotlin.models.AsyncResponse

class ImageAsyncTask<Response>(view: AsyncObject<Response>,
                               response: AsyncResponse<Response>) : AsyncTask<Void, Int, Response>() {
    private val view: AsyncObject<Response> = view
    private val response: AsyncResponse<Response> = response

    override fun doInBackground(vararg params: Void?): Response {
        return view.process()
    }

    override fun onPostExecute(result: Response) {
        response.processFinish(result)
    }
}
