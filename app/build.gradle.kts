plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.sahngha"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sahngha"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-storage:21.0.1") //thuvien
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.firebase:firebase-database") //dòng này để dùng firebase realtime db
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //Khai báo thêm thư viện
    implementation("com.google.android.material:material:1.10.0")
    //Khai báo cloudinary để lưu ảnh
    implementation("com.cloudinary:cloudinary-android:2.4.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
}