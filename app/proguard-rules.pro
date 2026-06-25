# Keep WebView JavaScript interface (if any)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep WebViewAssetLoader
-keep class androidx.webkit.** { *; }
