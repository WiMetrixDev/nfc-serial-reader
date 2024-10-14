package expo.modules.nfcserialreader

import android.util.Log
import com.rfid.reader.Reader
import java.io.File

class NFCReaderService(
    module: NFCSerialReaderModule,
) {
    val nfcSerialReaderModule = module
    var reader: Reader? = null
    val key: ByteArray =
        byteArrayOf(
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
        )
    val readerArgOne = 0x05.toByte()
    val readerArgTwo = 0x03.toByte()
    val readerArgThree = 0x00.toByte()
    val hexChars = "0123456789ABCDEF".toCharArray()
    val cardTypes =
        mapOf(
            0 to "TAG",
            5 to "WORKER",
        )
    companion object {
        private const val TAG = "NFCSerialReader"
    }

    // Extend the ByteArray class to add new function to convert byte array to hex string
    fun ByteArray.toHex(): String {
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(hexChars[firstIndex])
            result.append(hexChars[secondIndex])
        }

        return result.toString()
    }

    fun connect(
        serialPort: String,
        baudRate: Int,
    ): Boolean {
        if (reader != null) {
            Log.i(TAG, "Reader is already connected to $serialPort at $baudRate baud rate")
            return true
        }

        try {
            Log.i(TAG, "Connecting to the reader at $serialPort with $baudRate baud rate")
            reader = Reader.getInstance(serialPort, baudRate)
            Log.i(
                TAG,
                "Successfully connected to the reader at $serialPort with $baudRate baud rate",
            )

            // Start reading from the reader
            Thread {
                while (true) {
                    // If the reader is null, then we break the loop and stop reading
                    // This ensures that we stop reading when the reader is disconnected
                    if (reader == null) break
                    read()
                    Thread.sleep(1000)
                }
            }.start()

            return true
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to connect to the reader at $serialPort with $baudRate baud rate: $e",
            )
            e.printStackTrace()
            return false
        }
    }

    fun disconnect(): Boolean {
        if (reader == null) {
            Log.i(TAG, "Reader is already disconnected")
            return true
        }

        try {
            Log.i(TAG, "Disconnecting from the reader")
            reader?.close()
            reader = null
            Log.i(TAG, "Successfully disconnected from the reader")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect from the reader: $e")
            e.printStackTrace()
            return false
        }
    }

    fun read() {
        try {
            val readData = ByteArray(48)
            val errorCode = ByteArray(1)
            val result =
                reader?.Iso14443a_Read(
                    readerArgOne,
                    readerArgTwo,
                    readerArgThree,
                    key,
                    readData,
                    errorCode,
                )

            // If the result is not 0, then there was no card to read, so we early return
            if (result != 0) return

            // Extract the card ID from the read data using bitwise operations
            val cardId =
                ((readData[3].toInt() and 0xFF) shl 24) or
                    ((readData[2].toInt() and 0xFF) shl 16) or
                    ((readData[1].toInt() and 0xFF) shl 8) or
                    (readData[0].toInt() and 0xFF)

            // Convert the read data to a hex string
            val hexData = readData.toHex()

            // Extract the card type from the read data
            val cardType = hexData.substring(9, 10).toInt(16)

            // Translate the card type to a human-readable string
            val cardTypeString = cardTypes[cardType] ?: "UNKNOWN"

            // If card type is 0, which
            val finalCardId =
                if (cardType == 0) {
                    hexData.substring(0, 8).toInt()
                } else {
                    cardId
                }

            Log.i(
                TAG,
                "Read card of type $cardType translated to $cardTypeString with ID $cardId",
            )

            // Send the event to the JS side with the card ID and card type
            nfcSerialReaderModule.sendEvent(
                "onRead",
                mapOf(
                    "cardId" to finalCardId,
                    "cardType" to cardTypeString,
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read from the reader, got error: $e")
            e.printStackTrace()
        }
    }
}
