package br.com.trabalhoarretado.routes

import br.com.trabalhoarretado.application.user.UserService
import br.com.trabalhoarretado.domain.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.toByteArray
import java.util.UUID

fun Route.userRoutes(userService: UserService) {
    route("/api/users/me") {
        post("/avatar") {
            val userId =
                UUID.fromString(
                    call
                        .principal<JWTPrincipal>()!!
                        .payload
                        .getClaim("userId")
                        .asString(),
                )

            var bytes: ByteArray? = null
            var contentType: String? = null

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem && part.name == "file") {
                    contentType = part.contentType?.toString()
                    bytes = part.provider().toByteArray()
                }
                part.dispose()
            }

            val fileBytes = bytes ?: throw ValidationException("Campo 'file' ausente no multipart")
            val type = contentType ?: throw ValidationException("Content-Type do arquivo ausente")

            val response = userService.uploadAvatar(userId, fileBytes, type)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
