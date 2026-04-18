package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.auth.AuthService
import br.com.trabalhoarretado.application.auth.LoginRequest
import br.com.trabalhoarretado.application.auth.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val response = authService.register(req)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val response = authService.login(req)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("jwt-auth") {
            get("/me") {
                val userId =
                    call
                        .principal<JWTPrincipal>()!!
                        .payload
                        .getClaim("userId")
                        .asString()
                val response = authService.getMe(UUID.fromString(userId))
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
