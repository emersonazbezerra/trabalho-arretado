package br.com.trabalhoarretado

import android.app.Application
import br.com.trabalhoarretado.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TrabalhoArretadoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TrabalhoArretadoApp)
            modules(appModule)
        }
    }
}
