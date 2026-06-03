package br.com.trabalhoarretado.di

import br.com.trabalhoarretado.application.auth.AuthService
import br.com.trabalhoarretado.application.favorite.FavoriteService
import br.com.trabalhoarretado.application.professional.ProfessionalService
import br.com.trabalhoarretado.application.review.ReviewService
import br.com.trabalhoarretado.application.service.ServiceOfferService
import br.com.trabalhoarretado.application.user.UserService
import br.com.trabalhoarretado.domain.favorite.FavoriteRepository
import br.com.trabalhoarretado.domain.professional.ProfessionalRepository
import br.com.trabalhoarretado.domain.review.ReviewRepository
import br.com.trabalhoarretado.domain.service.ServiceOfferRepository
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.infra.db.repositories.FavoriteRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.ProfessionalRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.ReviewRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.ServiceOfferRepositoryImpl
import br.com.trabalhoarretado.infra.db.repositories.UserRepositoryImpl
import br.com.trabalhoarretado.infra.storage.ImageStorage
import br.com.trabalhoarretado.infra.storage.S3Config
import br.com.trabalhoarretado.infra.storage.S3ImageStorage
import org.koin.dsl.module

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expiresInDays: Long,
)

fun appModule(
    jwtConfig: JwtConfig,
    s3Config: S3Config,
) = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<ProfessionalRepository> { ProfessionalRepositoryImpl() }
    single<ServiceOfferRepository> { ServiceOfferRepositoryImpl() }
    single<FavoriteRepository> { FavoriteRepositoryImpl() }
    single<ReviewRepository> { ReviewRepositoryImpl() }

    single<ImageStorage> { S3ImageStorage(s3Config) }

    single { AuthService(get(), jwtConfig) }
    single { ProfessionalService(get(), get(), get()) }
    single { ServiceOfferService(get()) }
    single { UserService(get(), get()) }
    single { FavoriteService(get(), get(), get()) }
    single { ReviewService(get(), get()) }
}
