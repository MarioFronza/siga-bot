package br.com.mariofronza.sigabot.extraction.connector

import okhttp3.OkHttpClient
import retrofit2.Retrofit

class Connector {

    fun createConnection(): RetrofitSigaConnector {
        val client = OkHttpClient.Builder()

        return Retrofit.Builder()
            .baseUrl("https://siga.udesc.br/")
            .client(client.build())
            .build()
            .create(RetrofitSigaConnector::class.java)
    }
}
