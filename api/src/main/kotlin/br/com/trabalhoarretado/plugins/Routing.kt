package br.com.trabalhoarretado.plugins

import br.com.trabalhoarretado.application.auth.AuthService
import br.com.trabalhoarretado.application.favorite.FavoriteService
import br.com.trabalhoarretado.application.professional.ProfessionalService
import br.com.trabalhoarretado.application.service.ServiceOfferService
import br.com.trabalhoarretado.application.user.UserService
import br.com.trabalhoarretado.domain.EmailAlreadyExistsException
import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.InvalidCredentialsException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.ValidationException
import br.com.trabalhoarretado.routes.authRoutes
import br.com.trabalhoarretado.routes.favoriteRoutes
import br.com.trabalhoarretado.routes.healthRoutes
import br.com.trabalhoarretado.routes.professionalRoutes
import br.com.trabalhoarretado.routes.serviceRoutes
import br.com.trabalhoarretado.routes.userRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiError,
)

fun Application.configureRouting() {
    install(StatusPages) {
        exception<EmailAlreadyExistsException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ApiErrorEnvelope(ApiError("EMAIL_ALREADY_EXISTS", cause.message!!)))
        }
        exception<InvalidCredentialsException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, ApiErrorEnvelope(ApiError("INVALID_CREDENTIALS", cause.message!!)))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ApiErrorEnvelope(ApiError("NOT_FOUND", cause.message!!)))
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ApiErrorEnvelope(ApiError("FORBIDDEN", cause.message!!)))
        }
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiErrorEnvelope(ApiError("VALIDATION_ERROR", cause.message!!)))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorEnvelope(ApiError("INTERNAL_ERROR", "Erro interno no servidor")),
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiErrorEnvelope(ApiError("NOT_FOUND", "Recurso não encontrado")),
            )
        }
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiErrorEnvelope(ApiError("UNAUTHORIZED", "Token ausente ou inválido")),
            )
        }
    }

    val authService: AuthService by inject()
    val professionalService: ProfessionalService by inject()
    val serviceOfferService: ServiceOfferService by inject()
    val userService: UserService by inject()
    val favoriteService: FavoriteService by inject()

    routing {
        healthRoutes()
        authRoutes(authService)
        authenticate("jwt-auth") {
            professionalRoutes(professionalService)
            serviceRoutes(serviceOfferService)
            userRoutes(userService)
            favoriteRoutes(favoriteService)
        }
    }
}
