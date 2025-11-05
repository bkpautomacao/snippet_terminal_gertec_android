package br.com.gabrielmorais.terminalgertec

import android.app.Application
import br.com.gabrielmorais.terminalgertec.di.mainModule
import br.com.gabrielmorais.terminalgertec.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BkpWayApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@BkpWayApplication)
      modules(mainModule, viewModelModule)
    }
  }

}