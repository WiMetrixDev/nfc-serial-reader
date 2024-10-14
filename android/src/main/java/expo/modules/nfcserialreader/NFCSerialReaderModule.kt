package expo.modules.nfcserialreader

import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.File

class NFCSerialReaderModule : Module() {
    companion object {
        private const val TAG = "NFCSerialReaderModule"
    }

    val nfcReader = NFCReaderService(this@NFCSerialReaderModule)
    val serialPorts = mutableListOf<String>()

    // Get list of available serial ports on the device
    fun listSerialPorts(): List<String> {
        if (serialPorts.isEmpty()) {
            val devDirectory = File("/dev")

            if (devDirectory.exists() && devDirectory.isDirectory) {
                val files = devDirectory.listFiles()

                if (files != null) {
                    for (file in files) {
                        if (file.name.startsWith("tty")) {
                            serialPorts.add(file.absolutePath)
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error Listing Serial Ports! /dev Directory does not exist!")
            }
        }

        return serialPorts
    }

    // Pre-defined baud rates
    fun listBaudRates() = listOf(9600, 19200, 38400, 57600, 115200)

    override fun definition() =
        ModuleDefinition {
            Name("NFCSerialReader")

            Events("onRead")

            Function("connect") { serialPort: String, baudRate: Int ->
                nfcReader.connect(serialPort, baudRate)
            }

            Function("listSerialPorts") {
                listSerialPorts()
            }

            Function("listBaudRates") {
                listBaudRates()
            }

            Function("disconnect") {
                nfcReader.disconnect()
            }
        }
}
