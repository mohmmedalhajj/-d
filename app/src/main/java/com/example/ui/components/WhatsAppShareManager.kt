package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WhatsAppShareManager(private val context: Context) {

    fun shareSupplierReport(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        remainingBalance: Double,
        transactions: List<String>
    ): Boolean {
        return try {
            val message = buildSupplierReportMessage(
                supplierName,
                phone,
                totalPurchases,
                totalPaid,
                remainingBalance,
                transactions
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun buildSupplierReportMessage(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        remainingBalance: Double,
        transactions: List<String>
    ): String {
        val currentDateTime = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale("ar", "SA")
        ).format(Date())

        val sb = StringBuilder()
        sb.append("وكالة طوفان الأقصى لأجود أنواع القات الصعدي\n")
        sb.append("لصاحبها / أحمد منصور\n\n")
        sb.append("التاريخ والوقت: $currentDateTime\n\n")
        sb.append("=====================================\n")
        sb.append("كشف حساب المورد\n")
        sb.append("=====================================\n\n")
        sb.append("اسم المورد: $supplierName\n")
        sb.append("رقم الهاتف: $phone\n\n")
        sb.append("الملخص المالي:\n")
        sb.append("─────────────────────\n")
        sb.append("إجمالي المشتريات: $totalPurchases ر.ي\n")
        sb.append("إجمالي المسدد: $totalPaid ر.ي\n")
        sb.append("الرصيد المتبقي: $remainingBalance ر.ي\n\n")
        sb.append("=====================================\n")
        sb.append("تفاصيل العمليات:\n")
        sb.append("=====================================\n\n")

        for (transaction in transactions) {
            sb.append("$transaction\n")
        }

        sb.append("\n=====================================\n")
        sb.append("تم إنشاء هذا الكشف تلقائياً من نظام وكالة طوفان الأقصى\n")

        return sb.toString()
    }

    fun shareWithCustomMessage(message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isWhatsAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
