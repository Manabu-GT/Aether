import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.bcv) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.mapsSecrets) apply false
  alias(libs.plugins.spotless)
}

subprojects {
  plugins.withId("org.jetbrains.kotlin.android") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
  }

  plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure<DetektExtension> {
      buildUponDefaultConfig = true
      allRules = false
      config.from(rootProject.files("config/detekt/detekt.yml"))
      basePath = rootProject.projectDir.absolutePath
      source.setFrom(
        files(
          "src/main/java",
          "src/main/kotlin",
          "src/test/java",
          "src/test/kotlin"
        )
      )
    }
  }

  // Hook detekt and spotlessCheck into `check` task
  fun hookCheckWhen(pluginId: String, taskName: String) {
    pluginManager.withPlugin(pluginId) {
      tasks.named("check").configure { dependsOn(taskName) }
    }
  }
  plugins.withId("com.android.library") {
    hookCheckWhen("io.gitlab.arturbosch.detekt", "detekt")
  }
  plugins.withId("com.android.application") {
    hookCheckWhen("io.gitlab.arturbosch.detekt", "detekt")
  }
}

apply(from = "$rootDir/gradle/scripts/code-formatting.gradle")
