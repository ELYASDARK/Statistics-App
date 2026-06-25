plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.uniteconomics.calculator"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.uniteconomics.calculator"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = false
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)

  // Material 3 (for Theme.Material3.DayNight.NoActionBar)
  implementation(libs.material)

  // Splash Screen compat (supports API 23+)
  implementation(libs.androidx.core.splashscreen)

  // WebKit for WebViewAssetLoader + WebSettingsCompat
  implementation(libs.androidx.webkit)

  // Testing dependencies
  testImplementation(libs.junit)
}
