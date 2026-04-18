package br.com.trabalhoarretado.di

import br.com.trabalhoarretado.application.auth.AuthService
import br.com.trabalhoarretado.application.professional.ProfessionalService
import br.com.trabalhoarretado.application.service.ServiceOfferService
import br.com.trabalhoarretado.domain.professional.ProfessionalRepository
import br.com.trabalhoarretado.domain.service.ServiceOfferRepository
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.infra.db.repositories.ProfessionalRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.ServiceOfferRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.UserRepositoryImpl
import org.koin.dsl.module

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expiresInDays: Long,
)

fun appModule(jwtConfig: JwtConfig) =
    module {
        single<UserRepository> { UserRepositoryImpl() }
        single<ProfessionalRepository> { ProfessionalRepositoryImpl() }
        single<ServiceOfferRepository> { ServiceOfferRepositoryImpl() }

        single { AuthService(get(), jwtConfig) }
        single { ProfessionalService(get(), get()) }
        single { ServiceOfferService(get()) }
    }
