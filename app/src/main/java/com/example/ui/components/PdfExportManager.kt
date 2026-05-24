package com.example.ui.components

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.KITKAT)
class PdfExportManager(private val context: Context) {

    fun createSupplierPdfReport(
        supplierName: String,
        phone: String,
        totalPurchases: Double,
        totalPaid: Double,
        remainingBalance: Double,
        transactions: List<String>,
        fileName: String = generateFileName(supplierName)
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = android.graphics.Paint().apply {
                textSize = 14f
                color = android.graphics.Color.BLACK
            }

            val headerPaint = android.graphics.Paint().apply {
                textSize = 18f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            val rightMargin = 560f
            var yPosition = 40f

            // Header
            canvas.drawText("وكالة طوفان الأقصى لأجود أنواع القات الصعدي", 20f, yPosition, headerPaint)
            yPosition += 30f
            canvas.drawText("لصاحبها / أحمد منصور", 20f, yPosition, paint)
            yPosition += 30f

            // Date and Time
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar", "SA")).format(Date())
            canvas.drawText("التاريخ والوقت: $currentDate", 20f, yPosition, paint)
            yPosition += 30f

            // Supplier Info
            canvas.drawText("بيانات المورد", 20f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText("الاسم: $supplierName", 20f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("الهاتف: $phone", 20f, yPosition, paint)
            yPosition += 30f

            // Financial Summary
            canvas.drawText("الملخص المالي", 20f, yPosition, headerPaint)
            yPosition += 25f
            canvas.drawText("إجمالي المشتريات: $totalPurchases ر.ي", 20f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("إجمالي المسدد: $totalPaid ر.ي", 20f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("الرصيد المتبقي: $remainingBalance ر.ي", 20f, yPosition, paint)
            yPosition += 30f

            // Transactions
            canvas.drawText("تفاصيل العمليات", 20f, yPosition, headerPaint)
            yPosition += 25f

            for (transaction in transactions.take(20)) {
                canvas.drawText(transaction, 20f, yPosition, paint)
                yPosition += 15f
                if (yPosition > 750) break
            }

            pdfDocument.finishPage(page)

            val documentsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "وكالة طوفان الأقصى")
            }

            if (!documentsDir!!.exists()) {
                documentsDir.mkdirs()
            }

            val file = File(documentsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateFileName(supplierName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeName = supplierName.replace("/", "_").replace("\\", "_")
        return "كشف_حساب_${safeName}_$timestamp.pdf"
    }
}
