package com.bachnn.customview.view.customView

import android.content.Context
import java.util.LinkedList

class NavigationConfig(val context: Context) {

    var navigations: MutableList<Navigation>? = null

    private var navigationGroupView: NavigationGroupView? = null
    init {

    }


    companion object {
        fun with(context: Context): NavigationConfig {
            return NavigationConfig(context)
        }
    }

    fun addNavigationCell(navigation: Navigation): NavigationConfig {
        if (this.navigations == null) {
            this.navigations = LinkedList()
        }
        this.navigations?.add(navigation)
        return this
    }


    fun setNavigationRoot(navigationGroupView: NavigationGroupView): NavigationConfig {
        this.navigationGroupView = navigationGroupView
        return this
    }


    fun setup() {
        if (navigations == null) {
            throw NavigationException("Navigation is null!")
        } else if (navigations?.size!! <= 1) {
            throw NavigationException("Navigation size is 0, please add navigation")
        } else if (navigationGroupView == null) {
            throw NavigationException("Navigation Group View is null !")
        } else {
            navigationGroupView?.configure(this)
        }
    }


}