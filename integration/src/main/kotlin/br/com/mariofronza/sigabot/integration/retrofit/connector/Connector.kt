package br.com.mariofronza.sigabot.integration.retrofit.connector

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class Connector {

    fun createConnection(): OneSignalConnector {
        val client = OkHttpClient.Builder()

        return Retrofit.Builder()
            .baseUrl("https://onesignal.com/api/v1/notifications/")
            .addConverterFactory(JacksonConverterFactory.create())
            .client(client.build())
            .build()
            .create(OneSignalConnector::class.java)
    }
}
