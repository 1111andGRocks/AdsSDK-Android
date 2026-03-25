package com.grocks.ads

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

internal fun renderPdfFromAssets(
    context: Context,
    assetPath: String,
    maxWidthPx: Int,
    maxHeightPx: Int,
): Bitmap? {
    return try {
        val safeName = assetPath.replace("/", "_")
        val tmp = File(context.cacheDir, "grocks_pdf_$safeName")
        if (!tmp.exists() || tmp.length() == 0L) {
            context.assets.open(assetPath).use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
        }
        ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                if (renderer.pageCount <= 0) return null
                renderer.openPage(0).use { page ->
                    val pageWidth = page.width.toFloat().coerceAtLeast(1f)
                    val pageHeight = page.height.toFloat().coerceAtLeast(1f)
                    val scale = minOf(maxWidthPx / pageWidth, maxHeightPx / pageHeight)
                    val w = (pageWidth * scale).toInt().coerceAtLeast(1)
                    val h = (pageHeight * scale).toInt().coerceAtLeast(1)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.TRANSPARENT)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}
