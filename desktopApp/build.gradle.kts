import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(dependencyNotation = projects.shared)

    implementation(dependencyNotation = compose.desktop.currentOs)
    implementation(dependencyNotation = libs.kotlinx.coroutinesSwing)

    implementation(dependencyNotation = libs.compose.uiToolingPreview)

    implementation(dependencyNotation = project.dependencies.platform(libs.koin.bom))
    implementation(dependencyNotation = libs.koin.core)
    implementation(dependencyNotation = libs.koin.compose)
    implementation(dependencyNotation = libs.koin.compose.viewmodel)
}

compose.desktop {
    application {
        mainClass = "com.kapps.watson.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kapps.watson"
            packageVersion = "1.0.0"
        }
    }
}