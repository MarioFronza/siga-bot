package br.com.mariofronza.sigabot.integration.retrofit.connector

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration

class Connector {

    fun createConnection(baseUrl: String, callTimeout: Duration): OneSignalConnector {
        val client = OkHttpClient.Builder()
            .callTimeout(callTimeout)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(client.build())
            .build()
            .create(OneSignalConnector::class.java)
    }
}
