package br.com.trabalhoarretado.data.remote

import br.com.trabalhoarretado.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header("Authorization") != null) return chain.proceed(original)
        val token = runBlocking { tokenStore.getToken() } ?: return chain.proceed(original)
        val authed =
            original
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        return chain.proceed(authed)
    }
}
