import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "com.kapps.watson.shared")
        }
    }

    jvm()

    androidLibrary {
        namespace = "com.kapps.watson.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(dependencyNotation = libs.compose.uiToolingPreview)
            implementation(dependencyNotation = libs.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(dependencyNotation = libs.compose.runtime)
            implementation(dependencyNotation = libs.compose.foundation)
            implementation(dependencyNotation = libs.compose.material3)
            implementation(dependencyNotation = libs.compose.ui)
            implementation(dependencyNotation = libs.compose.components.resources)
            implementation(dependencyNotation = libs.compose.uiToolingPreview)
            implementation(dependencyNotation = libs.androidx.lifecycle.viewmodelCompose)
            implementation(dependencyNotation = libs.androidx.lifecycle.runtimeCompose)

            implementation(dependencyNotation = project.dependencies.platform(libs.koin.bom))
            implementation(dependencyNotation = libs.koin.core)
            implementation(dependencyNotation = libs.koin.compose)
            implementation(dependencyNotation = libs.koin.compose.viewmodel)

            implementation(dependencyNotation = libs.kotlinx.serialization.json)
            implementation(dependencyNotation = libs.kotlinx.coroutines.core)
            implementation(dependencyNotation = libs.kotlinx.datetime)

            implementation(dependencyNotation = libs.ktor.client.core)
            implementation(dependencyNotation = libs.ktor.client.contentNegotiation)
            implementation(dependencyNotation = libs.ktor.client.logging)
            implementation(dependencyNotation = libs.ktor.serialization.kotlinxJson)
            implementation(dependencyNotation = libs.ktor.client.encoding)
        }

        commonTest.dependencies {
            implementation(dependencyNotation = libs.koin.test)
            implementation(dependencyNotation = libs.kotlin.test)
            implementation(dependencyNotation = libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            implementation(dependencyNotation = libs.ktor.client.darwin)
        }

        jvmMain.dependencies {
            implementation(dependencyNotation = libs.ktor.client.cio)
        }
    }
}

dependencies {
    androidRuntimeClasspath(dependencyNotation = libs.compose.uiTooling)
}