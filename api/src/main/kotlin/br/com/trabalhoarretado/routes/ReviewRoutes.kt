package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.review.CreateReviewRequest
import br.com.trabalhoarretado.application.review.ReviewService
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.plugins.ApiError
import br.com.trabalhoarretado.plugins.ApiErrorEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.reviewRoutes(reviewService: ReviewService) {
    route("/api/professionals/{id}/reviews") {
        get {
            val professionalId =
                call.parameters["id"]?.let(UUID::fromString)
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "ID obrigatório")),
                    )
            call.respond(HttpStatusCode.OK, reviewService.list(professionalId))
        }

        post {
            val (clientId, role) = call.callerIdAndRole()
            val professionalId =
                call.parameters["id"]?.let(UUID::fromString)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "ID obrigatório")),
                    )
            val req = call.receive<CreateReviewRequest>()
            val response = reviewService.create(clientId, role, professionalId, req)
            call.respond(HttpStatusCode.Created, response)
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.callerIdAndRole(): Pair<UUID, UserRole> {
    val principal = principal<JWTPrincipal>()!!
    val id = UUID.fromString(principal.payload.getClaim("userId").asString())
    val role = UserRole.valueOf(principal.payload.getClaim("role").asString())
    return id to role
}
