package br.com.trabalhoarretado

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
    install(Koin) {
        slf4jLogger()
    }
    configureSerialization()
    configureDatabase()
    configureAuthentication()
    configureRouting()
}
