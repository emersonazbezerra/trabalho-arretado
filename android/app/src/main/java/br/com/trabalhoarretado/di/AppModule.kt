package br.com.trabalhoarretado.di

import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.data.remote.AuthAuthenticator
import br.com.trabalhoarretado.data.remote.AuthInterceptor
import br.com.trabalhoarretado.data.remote.NetworkModule
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.data.repository.FavoriteRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.data.repository.ReviewRepository
import br.com.trabalhoarretado.data.repository.ServiceRepository
import br.com.trabalhoarretado.data.repository.UserRepository
import br.com.trabalhoarretado.ui.auth.AuthViewModel
import br.com.trabalhoarretado.ui.favorites.FavoritesViewModel
import br.com.trabalhoarretado.ui.home.HomeViewModel
import br.com.trabalhoarretado.ui.professional.ProfessionalProfileViewModel
import br.com.trabalhoarretado.ui.professional.PublishServiceViewModel
import br.com.trabalhoarretado.ui.profile.MyProfileViewModel
import br.com.trabalhoarretado.ui.search.SearchViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule =
    module {
        single { TokenStore(androidContext()) }

        single { AuthInterceptor(get()) }
        single { AuthAuthenticator(get()) }
        single<OkHttpClient> { NetworkModule.provideOkHttp(get(), get()) }
        single<Retrofit> { NetworkModule.provideRetrofit(get()) }
        single<ApiService> { NetworkModule.provideApiService(get()) }

        single { AuthRepository(get(), get()) }
        single { ProfessionalRepository(get()) }
        single { FavoriteRepository(get()) }
        single { ServiceRepository(get()) }
        single { UserRepository(get()) }
        single { ReviewRepository(get()) }

        viewModel { AuthViewModel(get()) }
        viewModel { HomeViewModel(get()) }
        viewModel { SearchViewModel(get()) }
        viewModel { ProfessionalProfileViewModel(get(), get(), get(), get()) }
        viewModel { FavoritesViewModel(get()) }
        viewModel { PublishServiceViewModel(get(), get(), get()) }
        viewModel { MyProfileViewModel(get(), get(), get(), get()) }
    }
