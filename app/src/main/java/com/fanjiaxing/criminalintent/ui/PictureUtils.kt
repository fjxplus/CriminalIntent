package com.fanjiaxing.criminalintent.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build

fun getScaledBitmap(path: String, activity: Activity): Bitmap{
    val size = Point()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        activity.windowManager.currentWindowMetrics.bounds.apply {
            size.x = this.centerX()
            size.y = this.centerY()
        }
        getScaledBitmap(path, size.x, size.y)
    }else {
        activity.windowManager.defaultDisplay.getSize(size)
        getScaledBitmap(path, size.x, size.y)
    }

}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap{
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth){
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale){
            heightScale
        }else{
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    return BitmapFactory.decodeFile(path, options)
}