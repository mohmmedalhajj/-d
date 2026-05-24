package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.data.SupplierTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SupplierShareUtils {

    fun generateSupplierReport(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        transactions: List<SupplierTransaction>
    ): String {
        val sb = StringBuilder()
        val currentDateTime = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale("ar", "SA")
        ).format(Date())

        sb.append("═══════════════════════════════════════════════\n")
        sb.append("وكالة طوفان الأقصى لأجود أنواع القات الصعدي\n")
        sb.append("لصاحبها / أحمد منصور\n")
        sb.append("═══════════════════════════════════════════════\n\n")

        sb.append("📅 التاريخ والوقت: $currentDateTime\n\n")

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📋 بيانات المورد:\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
        sb.append("👤 اسم المورد: $supplierName\n")
        sb.append("☎️  رقم الهاتف: $phone\n\n")

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 الملخص المالي:\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
        sb.append("📊 إجمالي المشتريات: ${String.format("%.2f", totalPurchases)} ر.ي\n")
        sb.append("✅ إجمالي المسدد: ${String.format("%.2f", totalPaid)} ر.ي\n")
        sb.append("⚠️  الرصيد المتبقي: ${String.format("%.2f", totalPurchases - totalPaid)} ر.ي\n\n")

        if (transactions.isNotEmpty()) {
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📝 تفاصيل العمليات:\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

            for (transaction in transactions.take(20)) {
                val type = if (transaction.type == "شراء") "🛒" else "💸"
                sb.append("$type ${transaction.date}: ${String.format("%.2f", transaction.amountPaid)} ر.ي")
                if (transaction.notes.isNotEmpty()) {
                    sb.append(" - ${transaction.notes}")
                }
                sb.append("\n")
            }
        }

        sb.append("\n═══════════════════════════════════════════════\n")
        sb.append("✨ تم إنشاء هذا الكشف تلقائياً من نظام وكالة طوفان الأقصى\n")
        sb.append("═══════════════════════════════════════════════\n")

        return sb.toString()
    }

    fun shareViaWhatsApp(context: Context, message: String): Boolean {
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

    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareViaIntent(context: Context, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(intent, "شارك الكشف عبر"))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generatePdfFileName(supplierName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeName = supplierName.replace("/", "_").replace("\\", "_").take(20)
        return "كشف_${safeName}_$timestamp.pdf"
    }

    fun formatCurrency(amount: Double): String {
        return String.format("%.2f", amount)
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar", "SA"))
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("ar", "SA"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar", "SA"))
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar", "SA"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar", "SA")).format(Date())
    }

    fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale("ar", "SA")).format(Date())
    }
}
