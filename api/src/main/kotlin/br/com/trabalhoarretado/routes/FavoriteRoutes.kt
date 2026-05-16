package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.favorite.FavoriteAddResult
import br.com.trabalhoarretado.application.favorite.FavoriteService
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.plugins.ApiError
import br.com.trabalhoarretado.plugins.ApiErrorEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.favoriteRoutes(favoriteService: FavoriteService) {
    route("/api/favorites") {
        get {
            val (clientId, role) = call.callerIdAndRole()
            val response = favoriteService.list(clientId, role)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/{profId}") {
            val (clientId, role) = call.callerIdAndRole()
            val profId =
                call.parameters["profId"]?.let(UUID::fromString)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "profId obrigatório")),
                    )
            val status =
                when (favoriteService.add(clientId, role, profId)) {
                    FavoriteAddResult.Created -> HttpStatusCode.Created
                    FavoriteAddResult.AlreadyExists -> HttpStatusCode.OK
                }
            call.respond(status)
        }

        delete("/{profId}") {
            val (clientId, role) = call.callerIdAndRole()
            val profId =
                call.parameters["profId"]?.let(UUID::fromString)
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "profId obrigatório")),
                    )
            favoriteService.remove(clientId, role, profId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.callerIdAndRole(): Pair<UUID, UserRole> {
    val principal = principal<JWTPrincipal>()!!
    val id = UUID.fromString(principal.payload.getClaim("userId").asString())
    val role = UserRole.valueOf(principal.payload.getClaim("role").asString())
    return id to role
}
