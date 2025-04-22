package com.bachnn.customview.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CurvedCell @JvmOverloads constructor(
    private val context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attr, defStyleAttr) {

    fun setIconBySource(source: Int) {
        setImageResource(source)
        scaleType = ScaleType.CENTER
    }

    fun setColorTin(color: Int) {
//        setBa
    }

}