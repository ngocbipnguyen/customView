package com.bachnn.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.bachnn.curvednavigationbottom.MenuItem
import com.bachnn.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        this.setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.notificationFragment,
                R.id.homeFragment,
                R.id.settingFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        val menuItem = arrayOf(
            MenuItem(
                R.drawable.ic_notification,
                R.drawable.avd_notification,
                R.id.notificationFragment,
                "Notifications"
            ),
            MenuItem(
                R.drawable.ic_home,
                R.drawable.avd_home,
                R.id.homeFragment,
                "Home"
            ),
            MenuItem(
                R.drawable.ic_settings,
                R.drawable.avd_settings,
                R.id.settingFragment,
                "Settings"
            )
        )

        val activeIndex = 1

        binding.bottomNav.setMenuItems(menuItem, activeIndex)
        binding.bottomNav.setupWithNavController(navController)


    }
}