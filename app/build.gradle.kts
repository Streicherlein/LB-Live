plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lblive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lblive"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {

    // AndroidX dependencies for UI components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.gridlayout)
    implementation(libs.material.v190)
    // OkHttp dependencies for network operations
    implementation(libs.okhttp)
    // AppCompat for backward compatibility
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    //Flexibox Layout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // RecyclerView für die Liste
    implementation(libs.androidx.recyclerview)
    // SwipeRefreshLayout für die Pull-to-Refresh-Funktion
    implementation(libs.androidx.swiperefreshlayout)
    // JSON Verarbeitung
    implementation(libs.json)

    //gson for saving
    implementation(libs.gson)
    // Für GridLayout, falls benötigt
    implementation(libs.androidx.lifecycle.runtime.ktx.v260)  // Für Lifecycle
    implementation(libs.androidx.constraintlayout)  // Für Layouts und ConstraintLayout, falls gewünscht

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging dependencies for UI
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
