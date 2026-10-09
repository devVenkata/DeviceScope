
package com.devicescope.app.presentation.usage

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.graphics.drawable.BitmapDrawable

@Composable
fun AppUsageIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val iconBitmap = remember(context, packageName) {
        try {
            val drawable = context.packageManager
                .getApplicationIcon(packageName)

            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth
                    .takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight
                    .takeIf { it > 0 } ?: 96

                val bitmap = android.graphics.Bitmap.createBitmap(
                    width,
                    height,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap.asImageBitmap(),
            contentDescription = packageName,
            modifier = modifier.size(44.dp)
        )
    }
}
