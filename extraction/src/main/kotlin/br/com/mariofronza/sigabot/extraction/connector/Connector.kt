package br.com.mariofronza.sigabot.extraction.connector

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.time.Duration

class Connector(
    private val baseUrl: String = BASE_URL,
    private val callTimeout: Duration = Duration.ofSeconds(30),
) {

    fun createConnection(): RetrofitSigaConnector {
        val client = OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(20))
            .callTimeout(callTimeout)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client.build())
            .build()
            .create(RetrofitSigaConnector::class.java)
    }

    companion object {
        const val BASE_URL = "https://siga.udesc.br/"
    }
}
