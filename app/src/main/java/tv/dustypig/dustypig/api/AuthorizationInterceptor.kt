package tv.dustypig.dustypig.api

import okhttp3.Interceptor
import okhttp3.Response
import tv.dustypig.dustypig.AuthManager

object AuthorizationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithHeader = chain.request()
            .newBuilder()
            .header(
                "Authorization", "Bearer " + AuthManager.currentToken
            ).build()
        return chain.proceed(requestWithHeader)
    }
}