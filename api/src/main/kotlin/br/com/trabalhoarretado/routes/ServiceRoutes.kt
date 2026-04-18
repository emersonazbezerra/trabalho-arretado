package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.service.CreateServiceOfferRequest
import br.com.trabalhoarretado.application.service.ServiceOfferService
import br.com.trabalhoarretado.application.service.UpdateServiceOfferRequest
import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.plugins.ApiError
import br.com.trabalhoarretado.plugins.ApiErrorEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

fun Route.serviceRoutes(serviceOfferService: ServiceOfferService) {
    route("/services") {
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val callerId = principal.payload.getClaim("userId").asString()
            val callerRole = principal.payload.getClaim("role").asString()

            if (callerRole != "PROFESSIONAL") {
                throw ForbiddenException()
            }

            val req = call.receive<CreateServiceOfferRequest>()
            val response = serviceOfferService.create(req, UUID.fromString(callerId))
            call.respond(HttpStatusCode.Created, response)
        }

        put("/{id}") {
            val id =
                call.parameters["id"]
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "ID obrigatório")),
                    )
            val callerId =
                call
                    .principal<JWTPrincipal>()!!
                    .payload
                    .getClaim("userId")
                    .asString()
            val req = call.receive<UpdateServiceOfferRequest>()
            val response = serviceOfferService.update(UUID.fromString(id), UUID.fromString(callerId), req)
            call.respond(HttpStatusCode.OK, response)
        }

        delete("/{id}") {
            val id =
                call.parameters["id"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "ID obrigatório")),
                    )
            val callerId =
                call
                    .principal<JWTPrincipal>()!!
                    .payload
                    .getClaim("userId")
                    .asString()
            serviceOfferService.delete(UUID.fromString(id), UUID.fromString(callerId))
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
