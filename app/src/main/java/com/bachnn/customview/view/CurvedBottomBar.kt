package com.bachnn.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.doOnLayout
import com.bachnn.customview.R


// construct some attr for paint (color, width, height)
// set list curvedItem, check if this/2 != 0 throw error
// draw color
class CurvedBottomBar @JvmOverloads constructor(
    private val context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attr, defStyleAttr) {

    // paint
    val fabPaint: Paint
    val navPaint: Paint


    val path: Path = Path()

    /*attr*/
    private var fabColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    private var navColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    private var fabWidth: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    // end attr

    private val fabWidthDefault: Int = resources.getDimensionPixelSize(R.dimen.fab_size_default)

    private val navHeightDefault: Int = resources.getDimensionPixelSize(R.dimen.nav_width_default)

    private val fabRadius: Int = fabWidthDefault / 2

    private val bottomNavOffsetY =
        navHeightDefault - resources.getDimensionPixelSize(R.dimen.fab_size_default)


    private val curveBottomOffset = resources.getDimensionPixelSize(R.dimen.curve_bottom_offset)

    //    private val curve
    private val fabTopOffset = resources.getDimensionPixelSize(R.dimen.fab_top_offset)

    private val fabMargin = navHeightDefault - fabWidthDefault - fabTopOffset - curveBottomOffset


    private val topControlX = fabRadius + fabRadius / 2

    private val topControlY = bottomNavOffsetY + fabRadius / 6

    private val bottomControlX = fabRadius + (fabRadius / 2)

    private val bottomControlY = fabRadius / 4
    private val curveHalfWidth = fabRadius * 2 + fabMargin

    private val centerY = fabWidthDefault / 2f + fabTopOffset
    private var centerX = -1f
    private var curCenterY = centerY

    private var menuWidthCell: Int = 0
    private var cellOffsetX: Int = 0

    // array menu items
    private var curvedItems: Array<Curved> = arrayOf()
    private lateinit var curvedCells: Array<CurvedCell>


    private val firstCurveStart = PointF()
    private val firstCurveEnd = PointF()
    private val firstCurveControlPoint1 = PointF()
    private val firstCurveControlPoint2 = PointF()

    // second bezier curve
    private val secondCurveStart = PointF()
    private val secondCurveEnd = PointF()
    private val secondCurveControlPoint1 = PointF()
    private val secondCurveControlPoint2 = PointF()


    private var selectedIndex: Int = -1
    private var fabIconIndex: Int = -1

    private var prevSelectedIndex = -1

    init {
        setBackgroundColor(Color.TRANSPARENT)

        navPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = navColor
        }

        fabPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = fabColor
        }

        context.theme.obtainStyledAttributes(
            attr,
            R.styleable.custom_navigation_bottom, // The name of your styleable
            0, // Default value for the second parameter
            0  // Default value for the third parameter
        ).apply {
            navColor = getColor(R.styleable.custom_navigation_bottom_navigation_color, navColor)
            fabColor = getColor(R.styleable.custom_navigation_bottom_fab_color, fabColor)
            fabWidth = getDimensionPixelSize(
                R.styleable.custom_navigation_bottom_fab_width,
                fabWidthDefault
            )
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    // setup list item
    fun setupMenuItem(curvedItems: Array<Curved>) {
        this.curvedItems = curvedItems
        curvedCells = Array(curvedItems.size) {
            CurvedCell(context)
        }

        val index = curvedItems.size / 2

        fabIconIndex = index
        selectedIndex = index
        initializeCurve(index)

        initializeNavBottom(index)

    }

    private fun initializeNavBottom(i: Int) {
        removeAllViews()
        val layoutBottom = LinearLayout(context)

        curvedItems.forEachIndexed { index, curved ->

            val curvedCell = curvedCells[index]
            curvedCell.setIconBySource(curved.icon)

            val layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT
            )

            if (index == i) {
                curvedCell.visibility = View.INVISIBLE
            }

            layoutParams.weight = 1f
            layoutBottom.addView(curvedCell, layoutParams)
        }

        val layoutBottomParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimension(
                com.bachnn.curvednavigationbottom.R.dimen.height
            ).toInt(),
            Gravity.BOTTOM
        )
        addView(layoutBottom, layoutBottomParams)
    }

    private fun initializeCurve(index: Int) {
        doOnLayout {
            menuWidthCell = width / curvedItems.size
            val offsetX = menuWidthCell * index
            centerX = offsetX + menuWidthCell / 2f
            computeCurve(offsetX, menuWidthCell)
        }
    }


    // draw curve in center in array of menu.
    private fun computeCurve(offsetX: Int, w: Int) {
        this.cellOffsetX = offsetX
        Log.e("computeCurve", "offsetX $offsetX ; w : $w")
        firstCurveStart.apply {
            x = (offsetX + (w / 2) - curveHalfWidth).toFloat() // -
            y = bottomNavOffsetY.toFloat()
        }
        Log.e("computeCurve", "firstCurveStart $firstCurveStart")
        Log.e("computeCurve", "curveHalfWidth $curveHalfWidth ; bottomNavOffsetY $bottomNavOffsetY")
        firstCurveEnd.apply {
            x = (offsetX + (w / 2)).toFloat()
            y = (navHeightDefault - curveBottomOffset).toFloat()
        }
        Log.e("computeCurve", "firstCurveEnd $firstCurveEnd")
        Log.e("computeCurve", "layoutHeight $navHeightDefault : layoutHeight $curveBottomOffset")
        firstCurveControlPoint1.apply {
            x = firstCurveStart.x + topControlX // 3/2/2 fab size
            y = topControlY.toFloat() // bottomNavOffsetY + 1/6/2 fab size
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
            x = (offsetX + (w / 2) + curveHalfWidth).toFloat()
            y = bottomNavOffsetY.toFloat()
        }
        secondCurveControlPoint1.apply {
            x = secondCurveStart.x + bottomControlX
            y = secondCurveStart.y - bottomControlY
        }

        secondCurveControlPoint2.apply {
            x = secondCurveEnd.x - topControlX
            y = topControlY.toFloat()
        }

        path.apply {
            reset()
            moveTo(0f, bottomNavOffsetY.toFloat())
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
            lineTo(width.toFloat(), bottomNavOffsetY.toFloat())
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h: Int =
            paddingTop + paddingBottom + resources.getDimensionPixelSize(com.bachnn.curvednavigationbottom.R.dimen.layout_height)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(centerX, curCenterY, fabWidthDefault /2f , fabPaint)

        val drawable = resources.getDrawable(curvedItems[fabIconIndex].icon)
        drawable.setBounds(
            (centerX - drawable.intrinsicWidth / 2).toInt(),
            (curCenterY - drawable.intrinsicHeight / 2).toInt(),
            (centerX + drawable.intrinsicWidth / 2).toInt(),
            (curCenterY + drawable.intrinsicHeight / 2).toInt()
        )
        drawable.draw(canvas)
        canvas.drawPath(path, navPaint)
    }

}