package com.bachnn.customview.view.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bachnn.customview.R
import java.util.LinkedList


/*
* theme color of navihation bar
* background of navigation bottom -> surfaceContainer
* color of text and icon selected -> onSecondaryContainer
* background of item selected -> secondaryContainer
* color of text and icon unselected -> onSurfaceVariant
* */
class NavigationGroupView  @JvmOverloads constructor(
    private val context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attr, defStyleAttr) {

    private val widthParentView: Int
    private val widthGroupView: Int

    private val normalWidthCell: Int
    private val mainWithCell: Int

    private val normalMargin: Int
    private val mainMargin: Int

    private var backgroundGroup: Int

    private val mainNavigationView: MainNavigationCell

    private val mainNavigation: Navigation


    private var groupView: CurvedNavigationGroupView

    private lateinit var normalNavigationCells: MutableList<NormalNavigationCell>

    private var config: NavigationConfig? = null


    private var selectedEmotion: Int = -1
    private var previousSelectedEmotion = -1

    init {
        setWillNotDraw(false)
        attr.let {
            val typeArray = context.obtainStyledAttributes(it, R.styleable.custom_navigation_bottom)
            normalWidthCell = dpToXp(
                typeArray.getDimension(
                    R.styleable.custom_navigation_bottom_custom_width_normal,
                    60f
                )
            )
            mainWithCell = dpToXp(
                typeArray.getDimension(
                    R.styleable.custom_navigation_bottom_custom_width_main,
                    65f
                )
            )
            backgroundGroup = typeArray.getInt(
                R.styleable.custom_navigation_bottom_background_group,
                R.color.backgroundNavigationBottom
            )
            widthParentView = dpToXp(
                typeArray.getDimension(
                    R.styleable.custom_navigation_bottom_custom_width_main,
                    150f
                )
            )

            normalMargin = dpToXp(
                typeArray.getDimension(
                    R.styleable.custom_navigation_bottom_normal_margin,
                    24f
                )
            )
            mainMargin = dpToXp(
                typeArray.getDimension(
                    R.styleable.custom_navigation_bottom_main_margin,
                    30f
                )
            )
        }

        widthGroupView = (widthParentView * 8) / 10
        this.setBackgroundColor(Color.TRANSPARENT)

        // main navigation cell
        mainNavigation = Navigation(R.drawable.baseline_add_24, "add navigation")
        mainNavigationView = MainNavigationCell(context)
        mainNavigationView.layoutParams = LayoutParams(
            mainWithCell,
            mainWithCell,
            Gravity.TOP or Gravity.CENTER
        )

        mainNavigationView.setIcon(mainNavigation)

        // group view
        groupView = CurvedNavigationGroupView(context)
        groupView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, widthParentView - widthGroupView, 0, 0)
        }
        normalNavigationCells = LinkedList()

        this.addView(groupView)
        this.addView(mainNavigationView)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }


    private fun dpToXp(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private var indexed: Int = -1
    fun configure(config: NavigationConfig) {
        // normal cells
        //
        groupView.invalidate()

        this.config = config
        selectedEmotion = -1

        groupView.weightSum = (config.navigations?.size)?.toFloat()!!

        indexed = config.navigations?.size?.div(2)!!

        config.navigations?.forEachIndexed { index, navigation ->
            val navigationCell = NormalNavigationCell(context)
            navigationCell.layoutParams = getDefaultLayoutParams(index)
//            navigationCell.weight
            navigationCell.setIcon(navigation)
            groupView.addView(navigationCell)
            normalNavigationCells.add(navigationCell)
        }
    }

    private fun getDefaultLayoutParams(index: Int): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            normalWidthCell,
            normalWidthCell,
            1f
        ).apply {
            gravity = Gravity.CENTER
        }

//        when (index) {
//            (indexed - 1) -> {
//                params.setMargins(normalMargin, normalMargin, normalMargin, normalMargin)
//            }
//
//            indexed -> {
//                params.setMargins(normalMargin, normalMargin, normalMargin, normalMargin)
//            }
//
//            else -> {
//                params.setMargins(normalMargin, normalMargin, normalMargin, normalMargin)
//            }
//        }

        return params
    }

    fun getScreenWidth(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.currentWindowMetrics.bounds.width()
        } else {
            context.resources.displayMetrics.widthPixels
        }
    }

}