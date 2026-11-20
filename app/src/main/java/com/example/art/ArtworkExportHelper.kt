package com.example.art

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import com.example.model.DiscoveryItem
import java.io.File
import java.io.FileOutputStream

object ArtworkExportHelper {

    fun renderArtworkBitmap(
        context: Context,
        item: DiscoveryItem,
        style: ArtStyleMode,
        variationSeed: Int,
        widthPx: Int = 1200,
        heightPx: Int = 780
    ): Bitmap {
        val imageBitmap = ImageBitmap(widthPx, heightPx)
        val canvas = Canvas(imageBitmap)
        val drawScope = CanvasDrawScope()
        val density = Density(context.resources.displayMetrics.density)

        drawScope.draw(
            density = density,
            layoutDirection = LayoutDirection.Rtl,
            canvas = canvas,
            size = Size(widthPx.toFloat(), heightPx.toFloat())
        ) {
            drawDiscoveryArtwork(
                item = item,
                requestedStyle = style,
                progress = 1f,
                variationSeed = variationSeed
            )
        }
        return imageBitmap.asAndroidBitmap()
    }

    fun saveArtworkToDevice(
        context: Context,
        item: DiscoveryItem,
        style: ArtStyleMode,
        variationSeed: Int
    ): Result<String> = runCatching {
        val bitmap = renderArtworkBitmap(context, item, style, variationSeed)
        val fileName = "solomon_${item.id}_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SolomonsRing")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("تعذر إنشاء ملف الصورة")
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: error("تعذر فتح مجرى الحفظ")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            "تم حفظ اللوحة في المعرض (Pictures/SolomonsRing)"
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            "تم حفظ اللوحة بنجاح"
        }
    }

    fun shareArtwork(
        context: Context,
        item: DiscoveryItem,
        style: ArtStyleMode,
        variationSeed: Int
    ): Result<Unit> = runCatching {
        val bitmap = renderArtworkBitmap(context, item, style, variationSeed)
        val shareDir = File(context.cacheDir, "shared_art").apply { mkdirs() }
        val file = File(shareDir, "solomon_${item.id}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val effective = resolveEffectiveArtStyle(style, item.category)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "لوحة «${item.title}» بأسلوب (${effective.labelAr}) من تطبيق خاتم سليمان"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "مشاركة الرسمة")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
