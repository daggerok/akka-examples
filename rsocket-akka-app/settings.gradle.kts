pluginManagement {
  repositories {
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
  }
  val lombokPluginVersion: String by extra
  val versionsPluginVersion: String by extra
  plugins {
    id("io.franzbecker.gradle-lombok") version lombokPluginVersion
    id("com.github.ben-manes.versions") version versionsPluginVersion
  }
}
