import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.11"
}

group = "tw.mingtsay"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("commons-io", "commons-io", "2.6")
    implementation("com.squareup.okhttp3", "okhttp", "3.12.0")
    implementation("com.beust", "klaxon", "3.0.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.3.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
