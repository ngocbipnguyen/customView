package com.bachnn.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView


class CurvedBottomNavigationView : BottomNavigationView {
    private var mPath: Path? = null
    private var mPaint: Paint? = null

    /** the CURVE_CIRCLE_RADIUS represent the radius of the fab button  */
    private val CURVE_CIRCLE_RADIUS = 256 / 2

    // the coordinates of the first curve
    private val mFirstCurveStartPoint = Point()
    private val mFirstCurveEndPoint = Point()
    private val mFirstCurveControlPoint1 = Point()
    private val mFirstCurveControlPoint2 = Point()

    //the coordinates of the second curve
    private var mSecondCurveStartPoint = Point()
    private val mSecondCurveEndPoint = Point()
    private val mSecondCurveControlPoint1 = Point()
    private val mSecondCurveControlPoint2 = Point()
    private var mNavigationBarWidth = 0
    private var mNavigationBarHeight = 0

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mPath = Path()
        mPaint = Paint()
        mPaint!!.style = Paint.Style.FILL_AND_STROKE
        mPaint!!.color = Color.RED
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // get width and height of navigation bar
        // Navigation bar bounds (width & height)
        mNavigationBarWidth = width
        mNavigationBarHeight = height
        Log.e("onSizeChanged", "WH : $width/$height")
        // the coordinates (x,y) of the start point before curve
        // todo x : -7/3 CURVE_CIRCLE_RADIUS y : 0
        mFirstCurveStartPoint[mNavigationBarWidth / 2 - CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS / 3] = 0
        Log.e("onSizeChanged", "mFirstCurveStartPoint $mFirstCurveStartPoint : ${mNavigationBarWidth / 2 - CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS / 3} = 0")
        // the coordinates (x,y) of the end point after curve
        //todo x: 1/2 width y : 5/4 CURVE_CIRCLE_RADIUS
        mFirstCurveEndPoint[mNavigationBarWidth / 2] = CURVE_CIRCLE_RADIUS + CURVE_CIRCLE_RADIUS / 4
        Log.e("onSizeChanged", "mFirstCurveEndPoint $mFirstCurveEndPoint  : ${mNavigationBarWidth / 2} = ${CURVE_CIRCLE_RADIUS + CURVE_CIRCLE_RADIUS / 4}")
        // same thing for the second curve
        //todo x: 1/2 width y : 5/4 CURVE_CIRCLE_RADIUS
        mSecondCurveStartPoint = mFirstCurveEndPoint
        Log.e("onSizeChanged", "mSecondCurveStartPoint $mSecondCurveStartPoint")
        //todo x : 7/3 CURVE_CIRCLE_RADIUS y: 0
        mSecondCurveEndPoint[mNavigationBarWidth / 2 + CURVE_CIRCLE_RADIUS * 2 + CURVE_CIRCLE_RADIUS / 3] = 0
        Log.e("onSizeChanged", "mSecondCurveEndPoint $mSecondCurveEndPoint")








        Log.e("onSizeChanged", "////")
        // the coordinates (x,y)  of the 1st control point on a cubic curve
        //todo x: 5/4 CURVE_CIRCLE_RADIUS = 160 y : 0
        mFirstCurveControlPoint1[mFirstCurveStartPoint.x + CURVE_CIRCLE_RADIUS + CURVE_CIRCLE_RADIUS / 4] =
            mFirstCurveStartPoint.y

        Log.e("onSizeChanged", "mFirstCurveControlPoint1 $mFirstCurveControlPoint1")
        // the coordinates (x,y)  of the 2nd control point on a cubic curve
        // todo x: -1 CURVE_CIRCLE_RADIUS = 412 y: 160
        mFirstCurveControlPoint2[mFirstCurveEndPoint.x - CURVE_CIRCLE_RADIUS * 2 + CURVE_CIRCLE_RADIUS] =
            mFirstCurveEndPoint.y
        Log.e("onSizeChanged", "mFirstCurveControlPoint2 $mFirstCurveControlPoint2")
        //todo x: + 1 CURVE_CIRCLE_RADIUS y: 160
        mSecondCurveControlPoint1[mSecondCurveStartPoint.x + CURVE_CIRCLE_RADIUS * 2 - CURVE_CIRCLE_RADIUS] =
            mSecondCurveStartPoint.y
        Log.e("onSizeChanged", "mSecondCurveControlPoint1 $mSecondCurveControlPoint1")

        //todo x: 5/4 CURVE_CIRCLE_RADIUS y: 0
        mSecondCurveControlPoint2[mSecondCurveEndPoint.x - (CURVE_CIRCLE_RADIUS + CURVE_CIRCLE_RADIUS / 4)] =
            mSecondCurveEndPoint.y
        Log.e("onSizeChanged", "mSecondCurveControlPoint2 $mSecondCurveControlPoint2")
        mPath?.reset()
        mPath?.moveTo(0f, 0f)
        mPath?.lineTo(mFirstCurveStartPoint.x.toFloat(), mFirstCurveStartPoint.y.toFloat())
        mPath?.cubicTo(
            mFirstCurveControlPoint1.x.toFloat(), mFirstCurveControlPoint1.y.toFloat(),
            mFirstCurveControlPoint2.x.toFloat(), mFirstCurveControlPoint2.y.toFloat(),
            mFirstCurveEndPoint.x.toFloat(), mFirstCurveEndPoint.y.toFloat()
        )
        mPath?.cubicTo(
            mSecondCurveControlPoint1.x.toFloat(), mSecondCurveControlPoint1.y.toFloat(),
            mSecondCurveControlPoint2.x.toFloat(), mSecondCurveControlPoint2.y.toFloat(),
            mSecondCurveEndPoint.x.toFloat(), mSecondCurveEndPoint.y.toFloat()
        )
        mPath?.lineTo(mNavigationBarWidth.toFloat(), 0f)
        mPath?.lineTo(mNavigationBarWidth.toFloat(), mNavigationBarHeight.toFloat())
        mPath?.lineTo(0f, mNavigationBarHeight.toFloat())
        mPath?.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath!!, mPaint!!)
    }
}