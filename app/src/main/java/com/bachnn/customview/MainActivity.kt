package com.bachnn.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.bachnn.customview.databinding.ActivityMainBinding
import com.bachnn.customview.view.customView.Navigation
import com.bachnn.customview.view.customView.NavigationConfig

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val config = NavigationConfig.with(this)
            .setNavigationRoot(binding.groupNavigation)
            .addNavigationCell(Navigation(R.drawable.baseline_add_24,""))
            .addNavigationCell(Navigation(R.drawable.baseline_add_24,""))
            .setup()

    }
}