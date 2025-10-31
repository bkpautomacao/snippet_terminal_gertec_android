package br.com.gabrielmorais.terminalgertec.di

import android.content.Context
import android.hardware.usb.UsbManager
import br.com.gabrielmorais.terminalgertec.UsbSerial
import org.koin.dsl.module

val mainModule = module {
    single {
        val usbManager = get<Context>().getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerial(usbManager)
    }
}