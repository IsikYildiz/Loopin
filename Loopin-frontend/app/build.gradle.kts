import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.51.1"
}

val properties_use = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties_use.load(FileInputStream(localPropertiesFile)) // This line should now resolve
}

android.buildFeatures.buildConfig = true
android {
    namespace = "com.example.loopin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.loopin"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = properties_use.getProperty("GOOGLE_MAPS_API_KEY", "YOUR_DEFAULT_API_KEY_IF_NOT_FOUND")
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.ui.text.android)
    implementation(libs.play.services.location)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    implementation(libs.retrofit) // Api istekleri için gerekli kütüphane
    implementation(libs.okhttp) // Http istekleri için gerekli kütüphane
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.converter.gson)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.calendarview) // Takvim için gerekli kütüphane
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation(libs.google.play.services.maps)
    implementation(libs.google.maps)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")
    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // ViewModel ve LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}