package com.example.ui.components

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThermalPrinterController(private val context: Context) {
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var selectedDevice: BluetoothDevice? = null

    companion object {
        private const val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val ESC = 0x1B.toChar()
        private const val GS = 0x1D.toChar()
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            if (hasPermission()) {
                bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun selectDevice(device: BluetoothDevice) {
        selectedDevice = device
    }

    fun getSelectedDevice(): BluetoothDevice? = selectedDevice

    suspend fun connectToPrinter(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!hasPermission()) {
                return@withContext false
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID))
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disconnectPrinter() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            outputStream = null
            bluetoothSocket = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun printSupplierReceipt(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        remainingBalance: Double,
        paperWidth: Int = 58 // 58mm or 80mm
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (outputStream == null) return@withContext false

            val content = buildSupplierReceiptContent(
                supplierName,
                phone,
                totalPurchases,
                totalPaid,
                remainingBalance,
                paperWidth
            )

            outputStream?.write(content.toByteArray(Charsets.UTF_8))
            outputStream?.flush()

            // Add feed and cut commands
            outputStream?.write(byteArrayOf(
                ESC.code.toByte(), 64.toByte() // Initialize printer
            ))
            outputStream?.write(byteArrayOf(27, 100, 3)) // Feed and cut (ESC d n)
            outputStream?.flush()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun buildSupplierReceiptContent(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        remainingBalance: Double,
        paperWidth: Int
    ): String {
        val sb = StringBuilder()
        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar", "SA")).format(Date())

        // Header
        sb.append(centerText("وكالة طوفان الأقصى", paperWidth)).append("\n")
        sb.append(centerText("لأجود أنواع القات الصعدي", paperWidth)).append("\n")
        sb.append(centerText("صاحبها / أحمد منصور", paperWidth)).append("\n\n")

        // Date and Time
        sb.append(rightAlignText("التاريخ: $currentTime", paperWidth)).append("\n")
        sb.append(repeatChar("-", paperWidth)).append("\n\n")

        // Supplier Info
        sb.append("المورد: $supplierName\n")
        sb.append("الهاتف: $phone\n\n")

        sb.append(repeatChar("-", paperWidth)).append("\n")
        sb.append("الملخص المالي\n")
        sb.append(repeatChar("-", paperWidth)).append("\n")

        sb.append(formatReceiptLine("المشتريات", "%.2f ر.ي".format(totalPurchases), paperWidth)).append("\n")
        sb.append(formatReceiptLine("المسدد", "%.2f ر.ي".format(totalPaid), paperWidth)).append("\n")
        sb.append(formatReceiptLine("المتبقي", "%.2f ر.ي".format(remainingBalance), paperWidth)).append("\n")

        sb.append(repeatChar("-", paperWidth)).append("\n\n")
        sb.append(centerText("شكراً لكم", paperWidth)).append("\n\n\n")

        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        val padding = (width - text.length) / 2
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else {
            text
        }
    }

    private fun rightAlignText(text: String, width: Int): String {
        val padding = width - text.length
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else {
            text
        }
    }

    private fun formatReceiptLine(label: String, value: String, width: Int): String {
        val totalLength = width
        val spacer = " ".repeat(totalLength - label.length - value.length)
        return "$label$spacer$value"
    }

    private fun repeatChar(char: String, times: Int): String {
        return (1..times).joinToString("") { char }
    }
}

data class BluetoothPrinterSettings(
    val deviceName: String,
    val deviceAddress: String,
    val paperWidth: Int = 58, // 58mm or 80mm
    val printDensity: Int = 5 // 0-8
)
