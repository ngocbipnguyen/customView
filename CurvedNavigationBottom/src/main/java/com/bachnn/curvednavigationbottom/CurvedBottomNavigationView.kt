package com.bachnn.curvednavigationbottom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import kotlin.math.abs


/*
* path to draw curve
* items icon invisible selected icon.
* a path draw selected icon.
* */
class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dotRadius: Float
    private var textSizeInSp: Float

    private val dotPaint: Paint
    private val textPaint: Paint
    private val fabPaint: Paint
    private val navPaint: Paint


    private val firstCurveStart = PointF()
    private val firstCurveEnd = PointF()
    private val firstCurveControlPoint1 = PointF()
    private val firstCurveControlPoint2 = PointF()

    // second bezier curve
    private val secondCurveStart = PointF()
    private val secondCurveEnd = PointF()
    private val secondCurveControlPoint1 = PointF()
    private val secondCurveControlPoint2 = PointF()

    private var isMenuInitialized = false

    // attr start
    private var textColor = Color.WHITE
        set(value) {
            field = value
            textPaint.color = value
            if (isMenuInitialized) {
                invalidate()
            }
        }

    private var dotSize = 5f.getScaledSize(context)
        set(value) {
            field = value
            dotRadius = dotSize
            if (isMenuInitialized) {
                invalidate()
            }
        }

    private var fontTextSize = 10f.getScaledSize(context)
        set(value) {
            field = value
            textPaint.textSize = value
            if (isMenuInitialized) {
                invalidate()
            }
        }

    private var dotColor = Color.WHITE
        set(value) {
            field = value
            dotPaint.color = value
            if (isMenuInitialized) {
                invalidate()
            }
        }

    private var showDot = true
        set(value) {
            field = value
            if (isMenuInitialized) {
                invalidate()
            }
        }

    private var selectedColor = Color.parseColor("#000000")
        set(value) {
            field = value
            if (isMenuInitialized) {
                updateMenuAVDsTint()
                invalidate()
            }
        }

    private var unSelectedColor = Color.parseColor("#8F8F8F")
        set(value) {
            field = value
            if (isMenuInitialized) {
                updateMenuIconsTint()
                invalidate()
            }
        }

    private val shadowColor: Int = Color.parseColor("#75000000")

    private var animDuration: Long = 300L

    private var fabElevation = 4.toPx(context).toFloat()
        set(value) {
            field = value
            fabPaint.setShadowLayer(fabElevation, 0f, 6f, shadowColor)
            if (isMenuInitialized) {
                invalidate()
            }

        }

    var navElevation = 6.toPx(context).toFloat()
        set(value) {
            field = value
            navPaint.setShadowLayer(navElevation, 0f, 6f, shadowColor)
            if (isMenuInitialized) {
                invalidate()
            }
        }


    var fabBackgroundColor = Color.WHITE
        set(value) {
            field = value
            fabPaint.color = value
            if (isMenuInitialized) {
                invalidate()
            }
        }

    var navBackgroundColor = Color.WHITE
        set(value) {
            field = value
            navPaint.color = value
            if (isMenuInitialized) {
                invalidate()
            }
        }
    // attr end

    private val path: Path = Path()

    // array item navigation item
    private var menuItems: Array<MenuItem> = arrayOf()
    private lateinit var bottomNavItemViews: Array<BottomNavItemView>
    private lateinit var menuIcons: Array<Drawable>
    private lateinit var menuAVDs: Array<AnimatedVectorDrawableCompat>

    // size of each cell compute in OnSizChanged()
    private var menuWidthCell: Int = 0

    private var cellOffsetX: Int = 0

    private var selectedIndex: Int = -1
    private var fabIconIndex: Int = -1

    private var prevSelectedIndex = -1

    private val fabSize = resources.getDimensionPixelSize(R.dimen.fab_size)

    private val layoutHeight = resources.getDimension(R.dimen.layout_height)

    private val bottomNavOffsetY = layoutHeight - resources.getDimensionPixelSize(R.dimen.height)

    private val curveBottomOffset = resources.getDimensionPixelOffset(R.dimen.bottom_curve_offset)

    private val fabRadius = resources.getDimension(R.dimen.fab_size) / 2

    private val fabTopOffset = resources.getDimension(R.dimen.fab_top_offset)

    private val fabMargin = layoutHeight - fabSize - fabTopOffset - curveBottomOffset

    private val topControlX = fabRadius + fabRadius / 2

    private val topControlY = bottomNavOffsetY + fabRadius / 6

    private val bottomControlX = fabRadius + (fabRadius / 2)

    private val bottomControlY = fabRadius / 4

    private val curveHalfWidth = fabRadius * 2 + fabMargin

    private val centerY = fabSize / 2f + fabTopOffset
    private var centerX = -1f
    private var curCenterY = centerY

    private var isAnimating = false

    private var menuItemClickListener: ((MenuItem, Int) -> Unit)? = null


    private var animatorSet = AnimatorSet()

    private val avdUpdateCallback = object : Drawable.Callback {
        override fun invalidateDrawable(who: Drawable) {
            this@CurvedBottomNavigationView.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        }
    }


    init {
        setBackgroundColor(Color.TRANSPARENT)

        dotRadius = dotSize
        textSizeInSp = fontTextSize

        navPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = navBackgroundColor
            setShadowLayer(navElevation, 0f, 6f, shadowColor)
        }

        textPaint = Paint().apply {
            color = textColor
            textSize = fontTextSize
        }

        dotPaint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }

        fabPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = fabBackgroundColor
            setShadowLayer(fabElevation, 0f, 6f, shadowColor)
        }

        context.theme.obtainStyledAttributes(attrs, R.styleable.CurveBottomNavigationBottom, 0, 0)
            .apply {
                try {


                    selectedColor =
                        getColor(
                            R.styleable.CurveBottomNavigationBottom_selected_color,
                            selectedColor
                        )
                    showDot = getBoolean(R.styleable.CurveBottomNavigationBottom_show_dot, showDot)
                    textColor =
                        getColor(R.styleable.CurveBottomNavigationBottom_text_color, textColor)
                    dotColor = getColor(R.styleable.CurveBottomNavigationBottom_dot_color, dotColor)
                    dotSize =
                        getDimension(R.styleable.CurveBottomNavigationBottom_dot_size, dotSize)
                    fontTextSize =
                        getDimension(
                            R.styleable.CurveBottomNavigationBottom_text_size,
                            fontTextSize
                        )
                    unSelectedColor = getColor(
                        R.styleable.CurveBottomNavigationBottom_unselect_color,
                        unSelectedColor
                    )
                    animDuration = getInteger(
                        R.styleable.CurveBottomNavigationBottom_animation_duration,
                        animDuration.toInt()
                    ).toLong()
                    fabBackgroundColor =
                        getColor(
                            R.styleable.CurveBottomNavigationBottom_fab_color,
                            fabBackgroundColor
                        )
                    navBackgroundColor = getColor(
                        R.styleable.CurveBottomNavigationBottom_nav_background,
                        navBackgroundColor
                    )
                    fabElevation = getDimension(
                        R.styleable.CurveBottomNavigationBottom_fab_elevation,
                        fabElevation
                    )
                    navElevation = getDimension(
                        R.styleable.CurveBottomNavigationBottom_nav_elevation,
                        navElevation
                    )
                } finally {
                    recycle()
                }
            }

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    fun isAnimating(): Boolean {
        return isAnimating
    }

    fun setMenuItems(menuItems: Array<MenuItem>, activeIndex: Int = 0) {
        if (menuItems.isEmpty()) {
            isMenuInitialized = false
            return
        }

        this.menuItems = menuItems
        fabIconIndex = activeIndex
        selectedIndex = activeIndex
        bottomNavItemViews = Array(menuItems.size) {
            BottomNavItemView(context)
        }

        initializeMenuIcons()
        initializeMenuAVDs()
        initializeCurve(activeIndex)
        initializeBottomItems(activeIndex)

        isMenuInitialized = true
        setupInitialAVD(activeIndex)
    }

    private fun setupInitialAVD(activeIndex: Int) {
        menuAVDs[activeIndex].callback = avdUpdateCallback
        menuAVDs[selectedIndex].start()
    }

    private fun initializeCurve(index: Int) {
        doOnLayout {
            menuWidthCell = width / menuItems.size
            val offsetX = menuWidthCell * index
            centerX = offsetX + menuWidthCell / 2f
            computeCurve(offsetX, menuWidthCell)
        }
    }

    private fun initializeMenuAVDs() {
        val activeColorFilter = PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
        menuAVDs = Array(menuItems.size) {
            val avd = AnimatedVectorDrawableCompat.create(context, menuItems[it].avdIcon)!!
            avd.colorFilter = activeColorFilter
            avd
        }
    }

    private fun initializeMenuIcons() {
        menuIcons = Array(menuItems.size) {
            val drawable =
                ResourcesCompat.getDrawable(resources, menuItems[it].icon, context.theme)!!
            DrawableCompat.setTint(drawable, unSelectedColor)
            drawable
        }
    }

    private fun initializeBottomItems(activeIndex: Int) {
        removeAllViews()
        val layoutBottom = LinearLayout(context)
        val typeValues = TypedValue()
        context.theme.resolveAttribute(
            androidx.appcompat.R.attr.selectableItemBackground,
            typeValues,
            true
        )
        menuIcons.forEachIndexed { index, icon ->
            val menuIcon = bottomNavItemViews[index]
            menuIcon.setMenuIconDrawable(icon)
            menuIcon.setOnClickListener {
                onMenuItemClick(index)
            }

            if (index == activeIndex) {
                menuIcon.visibility = View.INVISIBLE
            }

            val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.weight = 1f

            layoutBottom.addView(menuIcon, layoutParams)
        }

        val bottomLayoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimension(R.dimen.height).toInt(),
            Gravity.BOTTOM
        )
        addView(layoutBottom, bottomLayoutParams)
    }

    private fun updateMenuAVDsTint() {
        val activeColorFilter = PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
        menuAVDs.forEach {
            it.colorFilter = activeColorFilter
        }
    }

    private fun updateMenuIconsTint() {
        menuIcons.forEach {
            DrawableCompat.setTint(it, unSelectedColor)
        }
    }

    private fun setOnMenuItemClickListener(menuItemClickListener: (MenuItem, Int) -> Unit) {
        this.menuItemClickListener = menuItemClickListener
    }

    fun setupWithNavController(navController: NavController) {
        if (!isMenuInitialized) {
            throw RuntimeException("initialize menu by call setupWithNavController fail!")
        }

        setOnMenuItemClickListener { menuItem, _ ->
//            navvi
            navigateToDestination(navController, menuItem)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            for (i in menuItems.indices) {
                if (matchDestination(destination, menuItems[i].destinationId)) {
                    if (selectedIndex != i && isAnimating) {
                        animatorSet.cancel()
                        false
                    }
                    onMenuItemClick(i)
                }
            }
        }


    }

    private fun matchDestination(destination: NavDestination, @IdRes destinationId: Int): Boolean {
        var currentDestination = destination
        while (currentDestination.id != destinationId && currentDestination.parent != null) {
            currentDestination = currentDestination.parent!!
        }

        return currentDestination.id == destinationId
    }

    private fun navigateToDestination(navController: NavController, item: MenuItem) {
        if (item.destinationId == -1) {
            throw RuntimeException("please set a valid id, unable the navigation!")
        }

        val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)
        builder.setPopUpTo(findStartDestination(navController.graph).id, false)
        val options = builder.build()
        try {
            navController.navigate(item.destinationId, null, options)
        } catch (e: IllegalArgumentException) {
            Log.e("IllegalArgumentException", e.toString())
        }
    }

    private fun findStartDestination(graph: NavGraph): NavDestination {
        var startDestination: NavDestination = graph
        while (startDestination is NavGraph) {
            startDestination = graph.findNode(graph.startDestinationId)!!
        }

        return startDestination
    }

    private fun onMenuItemClick(index: Int) {
        if (selectedIndex == index) {
            return
        }
        if (isAnimating) {
            return
        }

        fabIconIndex = selectedIndex
        menuAVDs[index].stop()
        prevSelectedIndex = selectedIndex
        selectedIndex = index
        bottomNavItemViews.forEachIndexed { index, bottomNavItemView ->
            if (prevSelectedIndex == index) {
                bottomNavItemView.visibility = View.VISIBLE
                bottomNavItemView.alpha = 0f
            }
        }
        val newOffsetX = menuWidthCell * index
        isAnimating = true
        animateItemSelection(newOffsetX, menuWidthCell, index)

        menuItemClickListener?.invoke(menuItems[index], index)
    }

    private fun animateItemSelection(offset: Int, width: Int, index: Int) {
        val finalCenterX = menuWidthCell * index + (menuWidthCell / 2f)
        val propertyOffset = PropertyValuesHolder.ofInt(PROPERTY_OFFSET, cellOffsetX, offset)
        val propertyCenterX = PropertyValuesHolder.ofFloat(PROPERTY_CENTER_X, centerX, finalCenterX)

        val isLTR = (prevSelectedIndex - index) < 0
        val diff = abs(prevSelectedIndex - index)
        val iconAnimSlot = animDuration / diff

        val curveBottomOffset = (curveHalfWidth * animDuration / this.width).toLong()

        val offsetAnimator = getBezierCurveAnimation(
            animDuration,
            width,
            iconAnimSlot,
            isLTR,
            index,
            curveBottomOffset,
            diff,
            propertyOffset,
            propertyCenterX
        )


        val fabYOffset = firstCurveEnd.y + fabRadius
        val halfAnimation = animDuration / 2

        val centerYAnimatorHide = hideFAB(fabYOffset)
        centerYAnimatorHide.duration = halfAnimation

        val centerYAnimatorShow = showFAB(fabYOffset, index)
        centerYAnimatorShow.startDelay = halfAnimation
        centerYAnimatorShow.duration = halfAnimation
        animatorSet = AnimatorSet()
        animatorSet.playTogether(centerYAnimatorHide, offsetAnimator, centerYAnimatorShow)
        animatorSet.interpolator = FastOutLinearInInterpolator()
        animatorSet.start()
    }

    private fun showFAB(
        fabYOffset: Float,
        index: Int
    ): ValueAnimator {
        val propertyCenterYReverse =
            PropertyValuesHolder.ofFloat(PROPERTY_CENTER_Y, fabYOffset, centerY)
        return ValueAnimator().apply {
            setValues(propertyCenterYReverse)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    menuAVDs[index].callback = avdUpdateCallback
                    menuAVDs[index].start()
                }

                override fun onAnimationEnd(animation: Animator) {
                    bottomNavItemViews[index].visibility = INVISIBLE
                    isAnimating = false
                }

            })

            addUpdateListener {
                val newCenterY = it.getAnimatedValue(PROPERTY_CENTER_Y) as Float
                curCenterY = newCenterY
                invalidate()
            }

        }
    }

    private fun hideFAB(fabYOffset: Float): ValueAnimator {
        val propertyCenterY = PropertyValuesHolder.ofFloat(PROPERTY_CENTER_Y, centerY, fabYOffset)
        return ValueAnimator().apply {
            setValues(propertyCenterY)
            addUpdateListener { animator ->
                val newCenterY = animator.getAnimatedValue(PROPERTY_CENTER_Y) as Float
                curCenterY = newCenterY
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    fabIconIndex = selectedIndex
                }
            })
        }
    }


    private fun getBezierCurveAnimation(
        slideAnimDuration: Long,
        width: Int,
        iconAnimSlot: Long,
        isLTR: Boolean,
        index: Int,
        curveBottomOffset: Long,
        diff: Int,
        vararg propertyOffset: PropertyValuesHolder
    ): ValueAnimator {
        return ValueAnimator().apply {
            setValues(*propertyOffset)
            duration = slideAnimDuration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    bottomNavItemViews.forEach {
                        it.resetAnimation()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    bottomNavItemViews.forEach { it.resetAnimation() }
                }
            })

            addUpdateListener { animator ->
                val newOffset = getAnimatedValue(PROPERTY_OFFSET) as Int
                computeCurve(newOffset, width)
                invalidate()

                centerX = getAnimatedValue(PROPERTY_CENTER_X) as Float
                val currentTime = animator.animatedFraction * slideAnimDuration

                var overIconIndex = ((currentTime + (iconAnimSlot)) / iconAnimSlot).toInt()
                if (isLTR) {
                    overIconIndex += prevSelectedIndex

                    if (overIconIndex > index) {
                        return@addUpdateListener
                    }
                } else {
                    overIconIndex = prevSelectedIndex - overIconIndex
                    if (overIconIndex < index) {
                        return@addUpdateListener
                    }
                }

                when {
                    overIconIndex == index -> {
                        bottomNavItemViews[index].startDestinationAnimation(curveBottomOffset)
                        if (diff == 1) {
                            bottomNavItemViews[prevSelectedIndex].startSourceAnimation(
                                slideAnimDuration
                            )
                        }
                    }

                    abs(overIconIndex - prevSelectedIndex) == 1 -> {
                        bottomNavItemViews[prevSelectedIndex].startSourceAnimation(slideAnimDuration)
                        bottomNavItemViews[overIconIndex].startIntermediateAnimation(
                            slideAnimDuration, curveBottomOffset
                        )
                    }

                    else -> {
                        bottomNavItemViews[overIconIndex].startIntermediateAnimation(
                            slideAnimDuration,
                            curveBottomOffset
                        )
                    }
                }
            }
        }
    }

    private fun computeCurve(offsetX: Int, w: Int) {
        this.cellOffsetX = offsetX
        Log.e("computeCurve", "offsetX $offsetX ; w : $w")
        firstCurveStart.apply {
            x = offsetX + (w / 2) - curveHalfWidth // -
            y = bottomNavOffsetY
        }
        Log.e("computeCurve", "firstCurveStart $firstCurveStart")
        Log.e("computeCurve", "curveHalfWidth $curveHalfWidth ; bottomNavOffsetY $bottomNavOffsetY")
        firstCurveEnd.apply {
            x = (offsetX + (w / 2)).toFloat()
            y = layoutHeight - curveBottomOffset
        }
        Log.e("computeCurve", "firstCurveEnd $firstCurveEnd")
        Log.e("computeCurve", "layoutHeight $layoutHeight : layoutHeight $curveBottomOffset")
        firstCurveControlPoint1.apply {
            x = firstCurveStart.x + topControlX // 3/2/2 fab size
            y = topControlY // bottomNavOffsetY + 1/6/2 fab size
        }
        Log.e("computeCurve", "firstCurveControlPoint1 $firstCurveControlPoint1")
        Log.e(
            "computeCurve",
            "firstCurveStart.x ${firstCurveStart.x} : topControlX $topControlX : $topControlY"
        )
        firstCurveControlPoint2.apply {
            x = firstCurveEnd.x - bottomControlX // 3/2/2 fab size
            y = firstCurveEnd.y - bottomControlY // 1/4/2 fab size
        }

        Log.e("computeCurve", "firstCurveEnd $firstCurveEnd")
        Log.e("computeCurve", "firstCurveControlPoint2 $firstCurveControlPoint2")
        Log.e(
            "computeCurve",
            "bottomControlX $bottomControlX : bottomControlY $bottomControlY"
        )
        secondCurveStart.set(firstCurveEnd.x, firstCurveEnd.y)
        secondCurveEnd.apply {
            x = offsetX + (w / 2) + curveHalfWidth
            y = bottomNavOffsetY
        }
        secondCurveControlPoint1.apply {
            x = secondCurveStart.x + bottomControlX
            y = secondCurveStart.y - bottomControlY
        }

        secondCurveControlPoint2.apply {
            x = secondCurveEnd.x - topControlX
            y = topControlY
        }

        path.apply {
            reset()
            moveTo(0f, bottomNavOffsetY)
            lineTo(firstCurveStart.x, firstCurveStart.y)
            cubicTo(
                firstCurveControlPoint1.x,
                firstCurveControlPoint1.y,
                firstCurveControlPoint2.x,
                firstCurveControlPoint2.y,
                firstCurveEnd.x,
                firstCurveEnd.y
            )
            cubicTo(
                secondCurveControlPoint1.x,
                secondCurveControlPoint1.y,
                secondCurveControlPoint2.x,
                secondCurveControlPoint2.y,
                secondCurveEnd.x,
                secondCurveEnd.y
            )
            lineTo(width.toFloat(), bottomNavOffsetY)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h: Int =
            paddingTop + paddingBottom + resources.getDimensionPixelSize(R.dimen.layout_height)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isMenuInitialized) {
            return
        }

        canvas.drawCircle(centerX, curCenterY, fabSize / 2f, fabPaint)

        menuAVDs[fabIconIndex].setBounds(
            (centerX - menuIcons[fabIconIndex].intrinsicWidth / 2).toInt(),
            (curCenterY - menuIcons[fabIconIndex].intrinsicHeight / 2).toInt(),
            (centerX + menuIcons[fabIconIndex].intrinsicWidth / 2).toInt(),
            (curCenterY + menuIcons[fabIconIndex].intrinsicHeight / 2).toInt()
        )

        menuAVDs[fabIconIndex].draw(canvas)

        canvas.drawPath(path, navPaint)

        for (i in bottomNavItemViews.indices) {
            val itemCenterX = menuWidthCell * i + (menuWidthCell / 2f)
            val textY = layoutHeight - 2
            if (i == selectedIndex && showDot) {
                canvas.drawCircle(itemCenterX, layoutHeight - 12, dotRadius, dotPaint)
            } else {
//                textPaint.textAlign = Paint.Align.CENTER
//                canvas.drawText(menuItems[i].title, itemCenterX, textY, textPaint)
            }
        }
    }

    companion object {
        private const val TAG = "CurvedBottomNavigation"
        private const val PROPERTY_OFFSET = "OFFSET"
        private const val PROPERTY_CENTER_Y = "CENTER_Y"
        private const val PROPERTY_CENTER_X = "CENTER_X"
    }
}