package br.com.trabalhoarretado.data.remote

import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.domain.AuthEvent
import br.com.trabalhoarretado.domain.AuthEvents
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator(
    private val tokenStore: TokenStore,
) : Authenticator {
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        // 401 nos endpoints de auth são credenciais inválidas, não token expirado.
        if (response.request.url.encodedPath.startsWith("/api/auth/")) return null
        runBlocking { tokenStore.clear() }
        AuthEvents.emit(AuthEvent.Unauthorized)
        return null
    }
}
