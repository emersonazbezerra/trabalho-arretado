package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.professional.ProfessionalService
import br.com.trabalhoarretado.application.professional.UpdateProfessionalRequest
import br.com.trabalhoarretado.plugins.ApiError
import br.com.trabalhoarretado.plugins.ApiErrorEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

fun Route.professionalRoutes(professionalService: ProfessionalService) {
    route("/professionals") {
        get {
            val category = call.request.queryParameters["category"]
            val city = call.request.queryParameters["city"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val response = professionalService.search(category, city, page)
            call.respond(HttpStatusCode.OK, response)
        }

        get("/{id}") {
            val id =
                call.parameters["id"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorEnvelope(ApiError("VALIDATION_ERROR", "ID obrigatório")),
                    )
            val response = professionalService.findById(UUID.fromString(id))
            call.respond(HttpStatusCode.OK, response)
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
            val req = call.receive<UpdateProfessionalRequest>()
            val response = professionalService.update(UUID.fromString(id), UUID.fromString(callerId), req)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
