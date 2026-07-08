package com.V2Skydivejump.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.UserEntity
import com.V2Skydivejump.app.database.entities.DzWaiverEntity
import com.V2Skydivejump.app.TimeUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout

class AndroidPdfManager(private val context: Context) : PdfManager {
    override fun generateJumpPdf(user: UserEntity, jump: JumpLogEntity, ratings: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        var y = 50f
        
        // App Logo (Uppermost Left)
        try {
            // Attempt to load logo from resources. 
            // In KMP with Compose Resources, this might be tricky to get as Bitmap.
            // We'll look for a common resource ID or load from assets.
            val logoResId = context.resources.getIdentifier("logo", "drawable", context.packageName)
            if (logoResId != 0) {
                val logoBitmap = BitmapFactory.decodeResource(context.resources, logoResId)
                if (logoBitmap != null) {
                    val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 60, 60, true)
                    canvas.drawBitmap(scaledLogo, 50f, 40f, paint)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Header - Jumper Info (Offset from Logo)
        val textLeftMargin = 130f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(user.name, textLeftMargin, 60f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("@${user.screenName ?: "no_screen_name"}", textLeftMargin, 80f, paint)
        canvas.drawText("License: ${user.licenseNumber}", textLeftMargin, 100f, paint)
        canvas.drawText("Ratings: $ratings", textLeftMargin, 120f, paint)
        
        y = 160f
        
        // Title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Individual Jump Log", 50f, y, paint)
        y += 30f
        
        paint.textSize = 16f
        canvas.drawText("Jump #${jump.jumpNumber}", 50f, y, paint)
        y += 40f
        
        paint.isFakeBoldText = false
        paint.textSize = 12f
        
        val data = listOf(
            "Date" to TimeUtils.formatEpochMillis(jump.date),
            "Dropzone" to (jump.dzName ?: "N/A"),
            "Location" to "${jump.dzLocation ?: ""}, ${jump.country ?: ""}",
            "Aircraft" to (jump.aircraftType ?: "N/A"),
            "Tail #" to (jump.aircraftTailNumber ?: "N/A"),
            "Type" to (jump.jumpType ?: "Fun Jump"),
            "" to "", // Spacer
            "Exit Altitude" to "${jump.exitAltitudeAgl} ft",
            "Deployment Altitude" to "${jump.deploymentAltitudeAgl} ft",
            "Freefall Time" to "${jump.freefallTimeSeconds} sec",
            "Max Speed" to "${jump.maxSpeedMph} mph",
            "Avg Speed" to "${jump.averageSpeedMph} mph",
            "" to "", // Spacer
            "Disciplines" to (jump.disciplines ?: "N/A"),
            "Landing Style" to (jump.landingStyles ?: "N/A"),
            "Weather" to (jump.weatherCondition ?: "N/A"),
            "" to "", // Spacer
            "Container" to (jump.containerId ?: "N/A"),
            "Main Canopy" to (jump.mainCanopyId ?: "N/A"),
            "Reserve Canopy" to (jump.reserveCanopyId ?: "N/A"),
            "" to "", // Spacer
            "Notes" to (jump.jumpNotes ?: ""),
            "" to "", // Spacer
            "Verified" to if (jump.isVerified) "Yes" else "No",
            "Verifier" to (jump.verifierName ?: "N/A"),
            "Verifier License" to (jump.verifierLicense ?: "N/A")
        )
        
        data.forEach { (label, value) ->
            if (label.isEmpty()) {
                y += 10f
            } else {
                paint.isFakeBoldText = true
                canvas.drawText("$label:", 50f, y, paint)
                paint.isFakeBoldText = false
                canvas.drawText(value, 200f, y, paint)
                y += 20f
            }
        }

        pdfDocument.finishPage(page)

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Jump_${jump.jumpNumber}_${jump.date}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    override fun generateWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity) {
        val file = createWaiverPdfFile(dzo, waiver)
        if (file != null) {
            Toast.makeText(context, "Waiver PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            openFile(file)
        }
    }

    override fun printWaiverPdf(dzo: UserEntity, waiver: DzWaiverEntity) {
        val file = createWaiverPdfFile(dzo, waiver)
        if (file != null) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
            val jobName = "Waiver_${waiver.title}"
            
            val printAdapter = object : android.print.PrintDocumentAdapter() {
                override fun onLayout(oldAttributes: android.print.PrintAttributes?, newAttributes: android.print.PrintAttributes?, cancellationSignal: android.os.CancellationSignal?, callback: LayoutResultCallback?, extras: android.os.Bundle?) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder(jobName)
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(pages: Array<out android.print.PageRange>?, destination: android.os.ParcelFileDescriptor?, cancellationSignal: android.os.CancellationSignal?, callback: WriteResultCallback?) {
                    var input: java.io.InputStream? = null
                    var output: java.io.OutputStream? = null
                    try {
                        input = java.io.FileInputStream(file)
                        output = java.io.FileOutputStream(destination?.fileDescriptor)
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } > 0) {
                            output.write(buf, 0, bytesRead)
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback?.onWriteFailed(e.message)
                    } finally {
                        input?.close()
                        output?.close()
                    }
                }
            }
            printManager.print(jobName, printAdapter, null)
        }
    }

    private fun createWaiverPdfFile(dzo: UserEntity, waiver: DzWaiverEntity): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val contentWidth = pageWidth - (2 * margin).toInt()
        
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

        var y = margin
        val centerX = (pageWidth / 2).toFloat()

        // Header - DZ Name & Location (Centered)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(dzo.dzName ?: "Dropzone", centerX, y + 20f, paint)
        y += 25f
        
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText(dzo.dzLocation ?: "", centerX, y + 15f, paint)
        y += 40f
        
        // Title (Centered)
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText(waiver.title, centerX, y + 20f, paint)
        y += 40f

        // Content with Text Wrapping (Justified for API 26+)
        val layoutBuilder = StaticLayout.Builder.obtain(waiver.content, 0, waiver.content.length, textPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutBuilder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        }
        
        val staticLayout = layoutBuilder.build()

        // Draw content, potentially across multiple pages
        var startLine = 0
        while (startLine < staticLayout.lineCount) {
            canvas.save()
            canvas.translate(margin, y)
            
            // Determine how many lines fit on this page
            var endLine = startLine
            while (endLine < staticLayout.lineCount) {
                val nextLineBottom = staticLayout.getLineBottom(endLine) - staticLayout.getLineTop(startLine)
                if (y + nextLineBottom > pageHeight - margin - 40f) {
                    break // Page limit reached
                }
                endLine++
            }
            
            // Draw the lines for this page
            if (endLine > startLine) {
                // We clip the canvas to only show the lines for this page
                val clipTop = staticLayout.getLineTop(startLine)
                val clipBottom = staticLayout.getLineBottom(endLine - 1)
                canvas.clipRect(0f, 0f, contentWidth.toFloat(), (clipBottom - clipTop).toFloat())
                canvas.translate(0f, -clipTop.toFloat())
                staticLayout.draw(canvas)
            }
            canvas.restore()
            
            startLine = endLine
            
            // If more content remains, start a new page
            if (startLine < staticLayout.lineCount) {
                drawFooter(canvas, pageWidth, pageHeight, margin)
                pdfDocument.finishPage(page)
                
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = margin // Reset Y for subsequent pages
            }
        }

        // Draw Final Footer
        drawFooter(canvas, pageWidth, pageHeight, margin)
        pdfDocument.finishPage(page)

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val safeTitle = waiver.title.replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val file = File(directory, "Waiver_${safeTitle}_${waiver.id}.pdf")

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate Waiver PDF", Toast.LENGTH_SHORT).show()
            null
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawFooter(canvas: Canvas, pageWidth: Int, pageHeight: Int, margin: Float) {
        val paint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Powered by V2 Skydive Jump", (pageWidth / 2).toFloat(), pageHeight - 20f, paint)
    }

    private fun openFile(file: File) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private var internalPdfManager: PdfManager? = null

fun initPdfManager(context: Context) {
    internalPdfManager = AndroidPdfManager(context)
}

actual fun getPdfManager(): PdfManager {
    return internalPdfManager ?: throw IllegalStateException("PdfManager not initialized")
}
