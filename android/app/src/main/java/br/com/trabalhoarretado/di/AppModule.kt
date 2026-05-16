package br.com.trabalhoarretado.di

import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.data.remote.AuthAuthenticator
import br.com.trabalhoarretado.data.remote.AuthInterceptor
import br.com.trabalhoarretado.data.remote.NetworkModule
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.ui.auth.AuthViewModel
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

        viewModel { AuthViewModel(get()) }
    }
