plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.bcv)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.screenshot)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

kotlin {
  explicitApi()
}

android {
  namespace = "com.ms.square.aether"
  compileSdk = libs.versions.androidCompileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.androidMinSdk.get().toInt()
  }

  testOptions {
    targetSdk = libs.versions.androidTargetSdk.get().toInt()
  }

  experimentalProperties["android.experimental.enableScreenshotTest"] = true

  buildTypes {
    release {
      isMinifyEnabled = false
      consumerProguardFiles("consumer-rules.pro")
    }
  }
}

dependencies {
  implementation(libs.androidx.annotation)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)

  // Coroutines
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)

  screenshotTestImplementation(libs.screenshot.validation.api)
  screenshotTestImplementation(libs.androidx.ui.tooling)
}
