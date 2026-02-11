plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.mapsSecrets)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

android {
  namespace = "com.ms.square.aether.sample"
  compileSdk = libs.versions.androidCompileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.ms.square.aether.sample"
    minSdk = libs.versions.androidMinSdk.get().toInt()
    targetSdk = libs.versions.androidTargetSdk.get().toInt()

    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  implementation(projects.aether)
  debugImplementation(libs.debugoverlay)

  implementation(libs.androidx.core.ktx)
  implementation(libs.material)

  // Lifecycle
  implementation(libs.androidx.lifecycle.runtime.ktx)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.activity.compose)
  implementation(libs.maps.compose)

  debugImplementation(libs.androidx.compose.ui.tooling)

  testImplementation(libs.junit4)
}

secrets {
  // To add your Maps API key to this project:
  // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
  // 2. Add this line, where YOUR_API_KEY is your API key:
  //        MAPS_API_KEY=YOUR_API_KEY
  propertiesFileName = "secrets.properties"
}
