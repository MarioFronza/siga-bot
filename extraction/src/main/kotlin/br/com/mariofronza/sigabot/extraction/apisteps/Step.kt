package br.com.mariofronza.sigabot.extraction.apisteps

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepSuccess
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import org.jsoup.Jsoup
import retrofit2.Response

abstract class Step {

    fun <T> Response<T>.getBody() = with(this) {
        if (isSuccessful) {
            val response = this.body()!!
            if (response is ResponseBody) {
                val responseBody = (response as ResponseBody)
                val responseBodyString = responseBody.stringNotClose()

                return@with StepSuccess(responseBodyString)
            }
            StepSuccess(body())
        } else {
            val errorBody = errorBody() ?: throw Exception("Empty error body")
            val errorMessage = errorBody.toString()
            StepError(errorMessage)
        }
    }

    fun <T> Response<T>.updateViewState(extractorContext: ExtractorContext): Response<T> {
        if (isSuccessful) {
            val response = this.body()!!
            if (response is ResponseBody) {
                val responseBody = (response as ResponseBody)
                val document = Jsoup.parse(responseBody.stringNotClose())
                val viewStateInput = document.getElementById("j_id1:javax.faces.ViewState:0")

                if (viewStateInput != null) {
                    extractorContext.viewState = viewStateInput.attr("value")
                }

                document.getElementById("csrfToken")?.let { extractorContext.csrfToken = it.attr("value") }
            }
        }
        return this
    }

    fun <T> Response<T>.updateSessionId(extractorContext: ExtractorContext, position: Int = 1): Response<T> {
        if (isSuccessful) {
            val jSessionHeader = this.headers().values("Set-Cookie")[position]
            extractorContext.headerSessionId = jSessionHeader.split(";").first()
        }
        return this
    }

    fun <T> Response<T>.updateCookies(
        extractorContext: ExtractorContext,
        ettsPosition: Int = 0,
        etpPosition: Int = 2
    ): Response<T> {
        if (isSuccessful) {
            val etts = this.headers().values("Set-Cookie")[ettsPosition]
            val etp = this.headers().values("Set-Cookie")[etpPosition]
            extractorContext.etts = etts.split(";").first()
            extractorContext.etp = etp.split(";").first()
        }
        return this
    }

    fun <T> Response<T>.updateEndpoint(extractorContext: ExtractorContext): Response<T> {
        if (isSuccessful) {
            val response = this.body()!!
            if (response is ResponseBody) {
                val responseBody = (response as ResponseBody)
                val document = Jsoup.parse(responseBody.stringNotClose())
                val redirectContent = document.getElementsByTag("redirect")
                val url = redirectContent.attr("url")
                extractorContext.url = url.substringAfter(BASE_URL)
            }
        }
        return this
    }

    private fun ResponseBody.stringNotClose(): String {
        val responseBody = this
        val responseBuffer = responseBody.source().buffer.clone()
        val responseBodyClone = responseBuffer.asResponseBody(responseBody.contentType(), responseBody.contentLength())
        return responseBodyClone.string()
    }

    companion object {
        const val BASE_URL = "https://siga.udesc.br/"
    }
}
