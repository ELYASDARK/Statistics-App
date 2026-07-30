# Keep WebView JavaScript interface (if any)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep WebViewAssetLoader
-keep class androidx.webkit.** { *; }

# Suppress R8 missing class warning for GuardedBy in Tink / other libraries
-dontwarn javax.annotation.concurrent.GuardedBy
