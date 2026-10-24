plugins {
    kotlin("jvm")
}

group = "com.example"
version = "0.1.0-SNAPSHOT"

val kspVersion: String = providers.gradleProperty("ksp.version").get()

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}
