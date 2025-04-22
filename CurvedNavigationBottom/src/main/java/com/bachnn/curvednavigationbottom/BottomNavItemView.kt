package com.bachnn.curvednavigationbottom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class BottomNavItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var isAnimating = false

    fun setMenuIconResource(icon: Int) {
        setImageResource(icon)
        scaleType = ScaleType.CENTER
    }

    fun setMenuIconDrawable(icon: Drawable) {
        setImageDrawable(icon)
        scaleType = ScaleType.CENTER
    }

    fun resetAnimation() {
        isAnimating = false
    }

    fun startIntermediateAnimation(time: Long, offset: Long) {
        if (isAnimating) {
            return
        }

        val hideAnimation = getIconHideAnimation(time)

        val showDuration = time - 2 * offset
        if (showDuration < 0) {
            return
        }

        val showAnimation = getIconShowAnimation(time - 2 * offset)
        showAnimation.startDelay = offset
        val set = AnimatorSet()
        set.playSequentially(hideAnimation, showAnimation)
        set.interpolator = FastOutSlowInInterpolator()
        set.start()
    }

    fun startSourceAnimation(time: Long) {
        if (isAnimating) {
            return
        }

        val showAnimation = getIconShowAnimation(time)
        showAnimation.interpolator = DecelerateInterpolator()
        showAnimation.start()
    }

    fun startDestinationAnimation(time: Long) {
        if (isAnimating) {
            return
        }

        val hideAnimation = getIconHideAnimation(time)
        hideAnimation.interpolator = DecelerateInterpolator()
        hideAnimation.start()
    }

    private fun getIconHideAnimation(time: Long): ValueAnimator {
        return ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
            duration = time
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    isAnimating = true
                }
            })
        }
    }

    private fun getIconShowAnimation(time: Long): ValueAnimator {
        val translateYProperty = PropertyValuesHolder.ofFloat("translationY", height * 0.2f, 0f)
        val alphaProperty = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        return ObjectAnimator.ofPropertyValuesHolder(this, alphaProperty, translateYProperty)
            .apply {
                duration = time
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        isAnimating = true
                    }
                })
            }
    }


}