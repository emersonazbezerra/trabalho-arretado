package br.com.trabalhoarretado.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
)

fun Route.healthRoutes() {
    route("/api") {
        get("/health") {
            call.respond(HttpStatusCode.OK, HealthResponse(status = "Tudo certo!"))
        }
    }
}
