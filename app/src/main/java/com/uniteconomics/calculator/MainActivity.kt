package com.uniteconomics.calculator

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewFeature

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setupImmersiveSystemBars()
        setupWebView()
        setupBackPressedCallback()
    }

    private fun setupImmersiveSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val defaultColor = ThemeResolver.getStatusBarColor("dark")
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.parseColor(defaultColor)
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.parseColor(defaultColor)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    private fun setupWebView() {
        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor(ThemeResolver.getStatusBarColor("dark")))
            visibility = View.INVISIBLE
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        configureWebViewSettings()
        setupThemeBridge()
        setupWebViewClient()

        webView.loadUrl("https://appassets.androidplatform.net/assets/calculator.html")

        setContentView(webView)
    }

    @Suppress("DEPRECATION")
    private fun configureWebViewSettings() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            allowContentAccess = false
            allowFileAccess = false
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, false)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupThemeBridge() {
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun updateTheme(theme: String) {
                runOnUiThread {
                    val isLight = theme == "light"
                    val colorHex = ThemeResolver.getStatusBarColor(theme)
                    window.statusBarColor = Color.parseColor(colorHex)
                    window.navigationBarColor = Color.parseColor(colorHex)
                    
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.isAppearanceLightStatusBars = isLight
                    controller.isAppearanceLightNavigationBars = isLight
                }
            }
        }, "Android")
    }

    private fun setupWebViewClient() {
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = createWebViewClient(assetLoader)
    }

    private fun createWebViewClient(assetLoader: WebViewAssetLoader) = object : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = true

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.postDelayed({ view.visibility = View.VISIBLE }, 100)
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

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }
}
