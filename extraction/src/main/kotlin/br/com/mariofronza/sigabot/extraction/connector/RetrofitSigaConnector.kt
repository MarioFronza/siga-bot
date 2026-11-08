package br.com.mariofronza.sigabot.extraction.connector

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitSigaConnector {

    @GET("sigaSecurityG5")
    fun loginPage(): Call<ResponseBody>

    @POST
    @Headers(
        "Cookie: : cookie_mentorweb_consent_accepted=YES"
    )
    @FormUrlEncoded
    fun signIn(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @FieldMap body: Map<String, String>
    ): Call<ResponseBody>

    @GET
    fun signInRedirect(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<ResponseBody>

    @POST("sigaSecurityG5/redirectApp.jsf")
    @FormUrlEncoded
    fun dashboardRedirect(
        @HeaderMap headers: Map<String, String>,
        @FieldMap body: Map<String, String>
    ): Call<ResponseBody>

    @GET
    fun dashboard(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<ResponseBody>

    @GET
    fun dashboardSemesterResults(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<ResponseBody>

    @GET
    fun semesterResults(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<ResponseBody>

    @POST("siga/com/executaconsultapersonaliz.do")
    @FormUrlEncoded
    fun semesterResultsWithPeriod(
        @HeaderMap headers: Map<String, String>,
        @FieldMap body: Map<String, String>
    ): Call<ResponseBody>
}
