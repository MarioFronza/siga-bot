package br.com.mariofronza.sigabot.integration.retrofit.connector

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.dto.OneSignalNotification
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface OneSignalConnector {

    @POST(".")
    fun createNotification(
        @HeaderMap headers: Map<String, String>,
        @Body body: OneSignalNotification
    ): Call<ResponseBody>
}
