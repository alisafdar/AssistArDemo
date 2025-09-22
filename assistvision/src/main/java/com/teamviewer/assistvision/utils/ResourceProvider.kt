package com.teamviewer.assistar.demo.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

class ResourceProvider(
    val context: Context
) {
    fun getString(
        @StringRes stringResId: Int
    ): String = context.getString(stringResId)

    fun getDrawableBitmap(
        @DrawableRes drawableRes: Int,
        bitmapOptions: BitmapFactory.Options = BitmapFactory.Options()
    ): Bitmap = BitmapFactory.decodeResource(
        context.resources,
        drawableRes,
        bitmapOptions
    )

    fun getUriFromRawRes(
        @RawRes rawRes: Int
    ): Uri =
        Uri
            .Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .path(rawRes.toString())
            .build()

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    fun getCacheDirectory() = context.cacheDir

    fun showToast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        )
            .show()
    }
}
