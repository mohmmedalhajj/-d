package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfGenerator {

    /**
     * Generates a structural PDF locally on the device and shares it.
     * All operations are offline-first. Handles Arabic RTL, logo layout, and Palestinian flag styling.
     */
    fun generateAndSharePdf(
        context: Context,
        reportTitle: String,
        headers: List<String>, // ordered logically right-to-left
        rows: List<List<String>> // values ordered right-to-left matching headers
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width
            val pageHeight = 842 // A4 height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            // 1. DRAW PALESTINIAN FLAG & "فلسطين حرة" (Top Left)
            val flagLeft = 30f
            val flagWidth = 60f
            val flagHeight = 36f
            val flagTop = 30f

            // Black stripe
            paint.color = Color.BLACK
            canvas.drawRect(flagLeft, flagTop, flagLeft + flagWidth, flagTop + flagHeight / 3f, paint)

            // White stripe
            paint.color = Color.WHITE
            canvas.drawRect(flagLeft, flagTop + flagHeight / 3f, flagLeft + flagWidth, flagTop + 2f * flagHeight / 3f, paint)

            // Green stripe
            paint.color = Color.parseColor("#009639")
            canvas.drawRect(flagLeft, flagTop + 2f * flagHeight / 3f, flagLeft + flagWidth, flagTop + flagHeight, paint)

            // Red Triangle
            paint.color = Color.parseColor("#E4312B")
            val trianglePath = android.graphics.Path().apply {
                moveTo(flagLeft, flagTop)
                lineTo(flagLeft + flagWidth * 0.35f, flagTop + flagHeight / 2f)
                lineTo(flagLeft, flagTop + flagHeight)
                close()
            }
            canvas.drawPath(trianglePath, paint)

            // "فلسطين حرة" text below flag
            paint.color = Color.parseColor("#E4312B")
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("فلسطين حرة", flagLeft + 5f, flagTop + flagHeight + 14f, paint)

            // 2. DRAW LOGO (Top Right)
            try {
                // Try and load theic_logo drawable we placed
                val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo)
                if (logoBitmap != null) {
                    val destRect = Rect(pageWidth - 110, 20, pageWidth - 30, 100)
                    canvas.drawBitmap(logoBitmap, null, destRect, null)
                }
            } catch (e: Exception) {
                // Fail-safe simple placeholder
                paint.color = Color.parseColor("#009639")
                canvas.drawCircle(pageWidth - 70f, 60f, 35f, paint)
                paint.color = Color.WHITE
                paint.textSize = 10f
                canvas.drawText("طوفان الأقصى", pageWidth - 105f, 64f, paint)
            }

            // 3. DRAW APP HEADER & DETAILS (Center/Right-aligned)
            drawRtlText(canvas, "وكالة طوفان الأقصى لأجود أنواع القات الصعدي", pageWidth - 130f, 40f, 15f, true)
            drawRtlText(canvas, "صاحبها / أحمد منصور", pageWidth - 130f, 62f, 12f, true)
            drawRtlText(canvas, reportTitle, pageWidth - 130f, 85f, 11f, false)

            paint.color = Color.parseColor("#009639")
            paint.strokeWidth = 2f
            canvas.drawLine(30f, 115f, pageWidth - 30f, 115f, paint)

            // 4. DRAW TABLE DATA
            val startY = 140f
            var currentY = startY
            val rowHeight = 24f

            // Compute columns from Right to Left
            val tableWidth = pageWidth - 60f
            val colCount = headers.size
            val colWidth = tableWidth / colCount

            // Draw Table Headers
            paint.color = Color.parseColor("#ECEFF1")
            canvas.drawRect(30f, currentY - 14f, pageWidth - 30f, currentY + 10f, paint)

            paint.color = Color.BLACK
            paint.strokeWidth = 1f
            canvas.drawLine(30f, currentY + 10f, pageWidth - 30f, currentY + 10f, paint)

            for (i in 0 until colCount) {
                // Draw headers from right to left
                val rightColX = pageWidth - 30f - (i * colWidth)
                drawRtlText(canvas, headers[i], rightColX - 5f, currentY, 11f, true)
            }

            currentY += rowHeight

            // Draw Rows
            rows.forEachIndexed { index, rowValues ->
                if (currentY + rowHeight > pageHeight - 50f) {
                    // Fail-safe simple break if too long
                    return@forEachIndexed
                }

                // Zebra striping
                if (index % 2 == 1) {
                    paint.color = Color.parseColor("#F5F7F8")
                    canvas.drawRect(30f, currentY - 14f, pageWidth - 30f, currentY + 10f, paint)
                }

                paint.color = Color.parseColor("#CCCCCC")
                canvas.drawLine(30f, currentY + 10f, pageWidth - 30f, currentY + 10f, paint)

                for (colIndex in 0 until colCount) {
                    val rightColX = pageWidth - 30f - (colIndex * colWidth)
                    val value = rowValues.getOrNull(colIndex) ?: ""
                    drawRtlText(canvas, value, rightColX - 5f, currentY, 10f, false)
                }
                currentY += rowHeight
            }

            // Footer
            paint.color = Color.parseColor("#009639")
            canvas.drawLine(30f, pageHeight - 45f, pageWidth - 30f, pageHeight - 45f, paint)
            drawRtlText(canvas, "تم تصدير هذا التقرير وتأمينه محلياً 100% - وكالة طوفان الأقصى للقات الصعدي", pageWidth / 2f + 160f, pageHeight - 30f, 9f, false, isCentered = true)

            pdfDocument.finishPage(page)

            // Save PDF to cache/files dir
            val cleanTitle = reportTitle.replace(" ", "_")
            val pdfFile = File(context.cacheDir, "${cleanTitle}_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            // Share PDF
            sharePdfFile(context, pdfFile)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء إنشاء PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawRtlText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        isBold: Boolean,
        isCentered: Boolean = false
    ) {
        val tp = TextPaint().apply {
            color = Color.BLACK
            textSize = size
            isAntiAlias = true
            isFakeBoldText = isBold
            textAlign = if (isCentered) Paint.Align.CENTER else Paint.Align.RIGHT
        }

        // Draw the text
        canvas.drawText(text, x, y, tp)
    }

    private fun sharePdfFile(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "مشاركة تقرير PDF عبر:").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Formats structured text shares for SMS & WhatsApp.
     * Starts strictly with the mandated headers and has no emojis.
     */
    fun shareViaText(context: Context, title: String, details: List<Pair<String, String>>, isSms: Boolean = false) {
        val sb = java.lang.StringBuilder()
        sb.append("وكالة طوفان الأقصى لأجود أنواع القات الصعدي\n")
        sb.append("صاحبها / أحمد منصور\n")
        sb.append("-----------------------------\n")
        sb.append(title).append("\n")
        sb.append("-----------------------------\n")

        details.forEach { (label, value) ->
            sb.append(label).append(": ").append(value).append("\n")
        }

        sb.append("-----------------------------\n")
        sb.append("تاريخ السند: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date()))

        val shareText = sb.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (isSms) {
                // Filter to message protocols if explicit SMS
                `package` = "com.android.mms"
            }
        }
        val chooser = Intent.createChooser(intent, "مشاركة السند عبر:").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback: Clear package lock to allow any matching text sharing app
            intent.`package` = null
            val fallbackChooser = Intent.createChooser(intent, "مشاركة السند عبر:").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackChooser)
            } catch (ex: Exception) {
                android.widget.Toast.makeText(context, "الرسائل أو التطبيقات المطلوبة غير متوفرة حالياً!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
