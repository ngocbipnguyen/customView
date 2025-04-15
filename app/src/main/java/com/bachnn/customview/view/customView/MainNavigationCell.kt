package com.bachnn.customview.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.bachnn.customview.R

class MainNavigationCell(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): NavigationBase(context, attrs, defStyleAttr) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.main_navigation, this, true)
    }

    fun setIcon(descriptionIcon: Navigation) {
        val iconImage: ImageView = findViewById(R.id.image_view)
        iconImage.setImageResource(descriptionIcon.drawable)
    }

}