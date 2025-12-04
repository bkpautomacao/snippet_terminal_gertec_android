package br.com.bkpautomacao.bkpreco_lite

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import br.com.bkpautomacao.bkpreco_lite.exceptions.UsbConnectionException
import br.com.bkpautomacao.bkpreco_lite.exceptions.UsbNotFoundException
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
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

    val probeTable = ProbeTable()
    probeTable.addProduct(0x1504, 0x5889, FtdiSerialDriver::class.java)
    probeTable.addProduct(0x5e0, 0x1200, FtdiSerialDriver::class.java)
    val prober = UsbSerialProber(probeTable)
    val fullList = availableDrivers + prober.findAllDrivers(usbManager)
    Log.i("UsbSerial", "AvailableDevice: ${fullList.size}")
    fullList.forEach { driver ->
      Log.i("UsbSerial", "Dispositivos FullList: ${driver?.device?.deviceName}")
    }
    if (fullList.isEmpty()) {
      throw UsbNotFoundException("Nenhum dispositivo usb encontrado")
    }

    val usbDriver = fullList[0]
    if (usbDriver == null) {
      throw UsbNotFoundException("Nenhum dispositivo usb encontrado")
    }

    return usbDriver
  }

  fun filterDevice(deviceList: Collection<UsbDevice>): UsbDevice {
    val patterns = listOf(
      "usb2.0-ser",
      "FT232R USB UART",
      "handheld barcode scanner",
      "symbol",
      "DataTraveler"
    )

    val device = deviceList.firstOrNull { device ->
      Log.i(
        "UsbSerial",
        "Dispositivos: $device | vendor ${device.vendorId} | product ${device.productId}"
      )
      val name = device.productName ?: return@firstOrNull false
      patterns.any { pattern -> name.lowercase().contains(pattern, ignoreCase = true) }
    } ?: throw UsbNotFoundException("Nenhum dispositivo autorizado encontrado")

    Log.i("UsbSerial", "Filter Device: ${device.productName}")
    return device
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