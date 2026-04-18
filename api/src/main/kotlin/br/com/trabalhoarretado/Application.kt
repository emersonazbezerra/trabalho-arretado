package br.com.trabalhoarretado

import br.com.trabalhoarretado.di.JwtConfig
import br.com.trabalhoarretado.di.appModule
import br.com.trabalhoarretado.plugins.configureAuthentication
import br.com.trabalhoarretado.plugins.configureDatabase
import br.com.trabalhoarretado.plugins.configureRouting
import br.com.trabalhoarretado.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val jwtConfig =
        JwtConfig(
            secret = environment.config.property("jwt.secret").getString(),
            issuer = environment.config.property("jwt.issuer").getString(),
            audience = environment.config.property("jwt.audience").getString(),
            expiresInDays =
                environment.config
                    .propertyOrNull("jwt.expiresInDays")
                    ?.getString()
                    ?.toLong() ?: 7L,
        )

    install(Koin) {
        slf4jLogger()
        modules(appModule(jwtConfig))
    }

    configureSerialization()
    configureDatabase()
    configureAuthentication()
    configureRouting()
}
