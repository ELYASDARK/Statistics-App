package com.uniteconomics.calculator

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Protect sensitive financial metrics from OS Recents thumbnails and screen capture
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Tapjacking & Overlay Attack Defense (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                android.app.Activity::class.java.getMethod("setHideOverlayWindows", Boolean::class.javaPrimitiveType)
                    .invoke(this, true)
            }
        }

        // Enable system-wide obscured touch rejection against transparent overlays
        window.decorView.filterTouchesWhenObscured = true

        setContent {
            CalculatorApp()
        }
    }
}
