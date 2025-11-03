package br.com.gabrielmorais.terminalgertec.di

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.room.Room
import br.com.gabrielmorais.terminalgertec.UsbSerial
import br.com.gabrielmorais.terminalgertec.http.source.BkpWayDatabase
import br.com.gabrielmorais.terminalgertec.http.source.repository.AppConfigRepository
import org.koin.dsl.module

val mainModule = module {
  single {
    val usbManager = get<Context>().getSystemService(Context.USB_SERVICE) as UsbManager
    UsbSerial(usbManager)
  }

  single {
    Room.databaseBuilder(
      get(),
      BkpWayDatabase::class.java,
      "bkpway"
    ).build()
  }

  single {
    AppConfigRepository(get())
  }
}