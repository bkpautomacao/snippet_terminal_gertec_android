package br.com.gabrielmorais.terminalgertec

import android.hardware.usb.UsbManager
import android.util.Log
import br.com.gabrielmorais.terminalgertec.exceptions.UsbConnectionException
import br.com.gabrielmorais.terminalgertec.exceptions.UsbNotFoundException
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class UsbSerial(
    private val usbManager: UsbManager
) {

    fun getAvailableDevice(): UsbSerialDriver {
        val availableDrivers: MutableList<UsbSerialDriver?> = UsbSerialProber
            .getDefaultProber()
            .findAllDrivers(usbManager)

        if (availableDrivers.isEmpty()) {
            throw UsbNotFoundException("Nenhum dispositivo usb encontrado")
        }

        Log.i("UsbSerial", "getAvailableDevice: ${availableDrivers[0]?.device}")
        val usbDriver = availableDrivers[0]
        if (usbDriver == null) {
            throw UsbNotFoundException("Nenhum dispositivo usb encontrado")
        }

        return usbDriver
    }

    fun openPortConnection(driver: UsbSerialDriver): UsbSerialPort {
        val connection = usbManager.openDevice(driver.device)
        if (connection == null) {
            throw UsbConnectionException("erro na conexão com dispositivo")
        }

        val port = driver.ports[0]
        port.open(connection)
        port.setParameters(
            9600,
            8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE
        )

        return port
    }

    fun closePortConnection(port: UsbSerialPort) {
        port.close()
    }

}