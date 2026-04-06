import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // TODO: Раскомментировать после добавления google-services.json в app/
    // alias(libs.plugins.google.services)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.ileader.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ileader.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase credentials loaded from local.properties (not tracked by git)
        // IMPORTANT: No hardcoded fallbacks — local.properties MUST exist with these keys
        buildConfigField("String", "SUPABASE_URL", "\"${localProps.getProperty("supabase.url") ?: error("supabase.url not set in local.properties")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps.getProperty("supabase.anon.key") ?: error("supabase.anon.key not set in local.properties")}\"")
        buildConfigField("String", "DEMO_PASSWORD", "\"${localProps.getProperty("demo.password") ?: error("demo.password not set in local.properties")}\"")
        buildConfigField("String", "DEMO_ADMIN_PASSWORD", "\"${localProps.getProperty("demo.admin.password") ?: error("demo.admin.password not set in local.properties")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Supabase
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)

    // Ktor (HTTP client for Supabase)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.utils)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DataStore (for caching & tokens)
    implementation(libs.datastore.preferences)

    // Material Icons Extended (2000+ icons)
    implementation(libs.material.icons.extended)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle ViewModel Compose
    implementation(libs.lifecycle.viewmodel.compose)

    // Coil (Image Loading)
    implementation(libs.coil.compose)

    // Accompanist
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.permissions)

    // Supabase Realtime (для чата)
    implementation(libs.supabase.realtime)

    // Firebase (TODO: раскомментировать после добавления google-services.json)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.messaging)
    // implementation(libs.firebase.analytics)

    // QR code generation (ZXing)
    implementation(libs.zxing.core)

    // CameraX (для сканирования QR)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ML Kit barcode scanning
    implementation(libs.mlkit.barcode.scanning)

    // Media3 (Video Player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
