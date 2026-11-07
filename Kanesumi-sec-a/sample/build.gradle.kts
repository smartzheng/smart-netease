plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.takahashirinta.kanesumi.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.takahashirinta.kanesumi.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(project(":kanesumi-core"))
    implementation(project(":kanesumi-anim"))
    implementation(project(":kanesumi-structure"))
    implementation(project(":kanesumi-controls"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Icon vectors only — no Material theme / components.
    // material-icons-core supplies ImageVectors accessed via Icons.*.
    implementation("androidx.compose.material:material-icons-core:1.7.5")

    debugImplementation(libs.compose.ui.tooling)
}
