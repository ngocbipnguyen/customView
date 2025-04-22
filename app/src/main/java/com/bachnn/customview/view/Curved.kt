package com.bachnn.customview.view

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class Curved(
    @DrawableRes
    val icon: Int,
    @IdRes
    var destinationId: Int = -1
)
