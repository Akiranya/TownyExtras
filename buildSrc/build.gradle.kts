plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val indraVersion = "3.0.1"
    implementation("net.kyori.indra", "net.kyori.indra.gradle.plugin", indraVersion)
    implementation("net.kyori.indra.git", "net.kyori.indra.git.gradle.plugin", indraVersion)
}
