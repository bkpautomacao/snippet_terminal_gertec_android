package br.com.bkpautomacao.bkpreco_lite.di

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.room.Room
import br.com.bkpautomacao.bkpreco_lite.UsbSerial
import br.com.bkpautomacao.bkpreco_lite.http.source.BkpWayDatabase
import br.com.bkpautomacao.bkpreco_lite.http.source.preferences.Preferences
import br.com.bkpautomacao.bkpreco_lite.http.source.repository.AppConfigRepository
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

  single { Preferences(get()) }
}