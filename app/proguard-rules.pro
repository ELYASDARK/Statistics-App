# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in getDefaultProguardFile("proguard-android-optimize.txt").

# Preserve Compose runtime metadata and annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Strip all diagnostic logs and stack traces in release builds
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Keep Coroutines internal exception handlers
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Obfuscate source file names and line numbers in stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Repackage all obfuscated classes to flatten architecture
-repackageclasses 'com.uniteconomics.calculator.internal'
