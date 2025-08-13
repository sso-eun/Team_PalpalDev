plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.dundun_hi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dundun_hi"
        minSdk = 24
        targetSdk = 35
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)


    //navigation
    implementation(libs.androidx.navigation.compose)

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.6")
    implementation(libs.coil.kt.coil.compose)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.8.6")

    // OkHttp 로깅 인터셉터 (HttpLoggingInterceptor)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    //네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.21.0")
    // 위에랑 겹쳐서 최종 버전만 남겨둠
    // implementation ("com.google.android.gms:play-services-location:20.0.0")
    implementation("io.github.fornewid:naver-map-compose:1.2.3")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")


    //이미지 로딩
    implementation(libs.coil.kt.coil.compose)

    implementation(libs.accompanist.permissions)

    //손글씨 인식
    implementation("com.google.mlkit:text-recognition:16.0.1")
    // 기본(라틴)
    implementation("com.google.mlkit:text-recognition-korean:16.0.1")
    implementation("com.google.mlkit:digital-ink-recognition:18.1.0")

}