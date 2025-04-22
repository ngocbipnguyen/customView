package com.bachnn.curvednavigationbottom

import android.content.Context
import android.util.DisplayMetrics

fun Float.getScaledSize(context: Context): Float {
    val metrics = context.resources.displayMetrics
    val widthDp = metrics.widthPixels / metrics.density
    val scaleFactor = widthDp / 360f
    return this * scaleFactor
}

fun Int.toPx(context: Context) =
    (this * context.resources.displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT