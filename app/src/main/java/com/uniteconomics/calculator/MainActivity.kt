package com.uniteconomics.calculator

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupBackPressedCallback()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by remember(systemDark) { mutableStateOf(systemDark) }
            AppTheme(darkTheme = darkTheme) {
                CalculatorScreen(
                    darkTheme = darkTheme,
                    onThemeToggle = { darkTheme = !darkTheme }
                )
            }
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                    finish()
                } else {
                    lastBackPressTime = System.currentTimeMillis()
                    Toast.makeText(
                        this@MainActivity,
                        "دووبارە پاشگەز بکە بۆ دەرچوون",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}
