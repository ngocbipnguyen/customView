package com.bachnn.customview.view.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.widget.LinearLayout
import com.bachnn.customview.R

class CurvedNavigationGroupView @JvmOverloads constructor(
    private val context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {
    private var path: Path? = null
    private var paint: Paint? = null

    private val CURVE_CIRCLE_RADIUS = 200 / 2

    // width height
    private var navigationWidth: Int = -1
    private var navigationHeight: Int = -1

    private val firstCurvedStartPoint = Point()
    private val firstCurvedEndPoint = Point()
    private val firstCurvedControlPoint1 = Point()
    private val firstCurveControlPoint2 = Point()

    private var secondCurveStartPoint = Point()
    private val secondCurveEndPoint = Point()
    private val secondCurveControlPoint1 = Point()
    private val secondCurveControlPoint2 = Point()


    init {
        path = Path()
        paint = Paint()
        paint?.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = context.getColor(R.color.md_theme_surfaceContainer)
            setBackgroundColor(Color.TRANSPARENT)
        }
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        navigationWidth = width
        navigationHeight = height

        // top to bottom.
        firstCurvedStartPoint[navigationWidth / 2 - CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS / 3] =
            0
        firstCurvedEndPoint[navigationWidth / 2] = CURVE_CIRCLE_RADIUS + CURVE_CIRCLE_RADIUS / 4

        // bottom to top
        secondCurveStartPoint = firstCurvedEndPoint
        secondCurveEndPoint[navigationWidth / 2 + CURVE_CIRCLE_RADIUS * 2 + CURVE_CIRCLE_RADIUS / 3] =
            0

        // control ?
        firstCurvedControlPoint1[firstCurvedStartPoint.x + CURVE_CIRCLE_RADIUS * 2 + CURVE_CIRCLE_RADIUS / 4] =
            firstCurvedStartPoint.y

        firstCurveControlPoint2[firstCurvedEndPoint.x - CURVE_CIRCLE_RADIUS * 2 + CURVE_CIRCLE_RADIUS] =
            firstCurvedEndPoint.y


        secondCurveControlPoint1[secondCurveStartPoint.x + CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS] =
            secondCurveStartPoint.y
        secondCurveControlPoint2[secondCurveEndPoint.x - CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS / 4] =
            secondCurveEndPoint.y

        path?.apply {
            reset()
            moveTo(0f, 0f)
            lineTo(firstCurvedStartPoint.x.toFloat(), firstCurvedStartPoint.y.toFloat())
            cubicTo(
                firstCurvedControlPoint1.x.toFloat(), firstCurvedControlPoint1.y.toFloat(),
                firstCurveControlPoint2.x.toFloat(), firstCurveControlPoint2.y.toFloat(),
                firstCurvedEndPoint.x.toFloat(), firstCurvedEndPoint.y.toFloat()
            )

            cubicTo(
                secondCurveControlPoint1.x.toFloat(), secondCurveControlPoint1.y.toFloat(),
                secondCurveControlPoint2.x.toFloat(), secondCurveControlPoint2.y.toFloat(),
                secondCurveEndPoint.x.toFloat(), secondCurveEndPoint.y.toFloat()
            )

            lineTo(navigationWidth.toFloat(), 0f)
            lineTo(navigationWidth.toFloat(), navigationHeight.toFloat())
            lineTo(0f, navigationHeight.toFloat())
            close()

        }


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path!!, paint!!)
    }

}