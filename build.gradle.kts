@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)


plugins {
    kotlin("multiplatform")
    kotlin("plugin.spring")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.rpc.plugin")
    id("org.jetbrains.compose")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.devtools.ksp")
}

group = "com.example"
version = "0.1.0-SNAPSHOT"

val composeVersion: String = providers.gradleProperty("compose.version").get()
val composeHtmlEapVersion: String = providers.gradleProperty("compose.html.eap.version").get()
val kotlinxBrowserCommonSubsetVersion: String =
    providers.gradleProperty("compose.html.eap.kotlinx-browser-common-subset.version").get()
val generatedWebResources = layout.buildDirectory.dir("generated/web-resources")
val kotlinxRpcVersion: String = providers.gradleProperty("kotlinx.rpc.version").get()
val kotlinxSerializationVersion: String = providers.gradleProperty("kotlinx.serialization.version").get()
val ktorVersion: String = providers.gradleProperty("ktor.version").get()

kotlin {
    jvm()
    wasmJs {
        outputModuleName = "search-client"
        browser()
        binaries.executable()
    }
    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
            implementation("org.jetbrains.compose.html.eap:html-core-eap:$composeHtmlEapVersion")
            implementation("org.jetbrains.compose.html:kotlinx-browser-common-subset:$kotlinxBrowserCommonSubsetVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-client:$kotlinxRpcVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json:$kotlinxRpcVersion")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-client:0.10.4")
        }


        jvmMain {
            resources.srcDir(rootProject.file("web-assets"))
            resources.srcDir(generatedWebResources)
            dependencies {
                runtimeOnly("androidx.compose.runtime:runtime-desktop:${composeVersion}")
                implementation("org.jetbrains.compose.html:kotlinx-browser-common-subset:${kotlinxBrowserCommonSubsetVersion}")
                implementation(kotlin("reflect"))
                implementation("org.springframework.boot:spring-boot-starter-web")
                implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-server:${kotlinxRpcVersion}")
                implementation("tools.jackson.core:jackson-databind")
                implementation("org.springframework.boot:spring-boot-starter-websocket")
                implementation(kotlin("reflect"))
            }
        }

    }
}

dependencies {
    add("kspJvm", project(":page-processor"))
}

val copyBrowserBundle = tasks.register<Sync>("copyBrowserBundle") {
    dependsOn(tasks.named("wasmJsProductionExecutableCompileSync"))
    val browserDistribution = layout.buildDirectory.dir(
        "compileSync/wasmJs/main/productionExecutable/optimized",
    )
    from(browserDistribution) {
        include("search-client*.mjs", "search-client.wasm")
    }
    into(generatedWebResources.map { it.dir("static") })
}

tasks.named("jvmProcessResources") {
    dependsOn(copyBrowserBundle)
}

tasks.register("test") {
    group = "verification"
    description = "Runs the tests for all targets."
    dependsOn("allTests")
}

val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")

tasks.register<JavaExec>("bootRun") {
    group = "application"
    description = "Runs the hydrated-search Spring Boot application."
    dependsOn("jvmMainClasses")
    mainClass.set("dev.frederikpietzko.example.MainKt")
    classpath(jvmMainCompilation.output.allOutputs)
    classpath(jvmMainCompilation.runtimeDependencyFiles)
}
