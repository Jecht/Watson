import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(dependencyNotation = projects.shared)

    implementation(dependencyNotation = libs.androidx.activity.compose)

    implementation(dependencyNotation = libs.compose.uiToolingPreview)

    implementation(dependencyNotation = project.dependencies.platform(libs.koin.bom))
    implementation(dependencyNotation = libs.koin.core)
    implementation(dependencyNotation = libs.koin.compose)
    implementation(dependencyNotation = libs.koin.compose.viewmodel)

    debugImplementation(dependencyNotation = libs.compose.uiTooling)
}

android {
    namespace = "com.kapps.watson"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kapps.watson"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}