package com.bachnn.curvednavigationbottom

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class MenuItem(
    @DrawableRes
    val icon: Int,
    @DrawableRes
    val avdIcon: Int,
    @IdRes
    val destinationId: Int = -1,
    val title: String = ""
)
