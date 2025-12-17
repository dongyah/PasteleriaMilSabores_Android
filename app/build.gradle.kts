plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.pasteleriamilsabores"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pasteleriamilsabores"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // dependencias de firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    //dependencias para Sing in with google
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")



    // --- DEPENDENCIAS MOSHI Y RETROFIT ---

    // Adaptadores de Moshi para Kotlin
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Núcleo de Retrofit (Para la API Service)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor JSON usando Moshi dentro de Retrofit
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")

    // Procesador de anotaciones para Moshi (KAPT)
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    // Interceptor de OkHttp para loguear requests/responses (Maneja el LoggingInterceptor)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Dependencias de CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // DEPENDENCIA DE GOOGLE GENERATIVE AI (GEMINI)
    implementation("com.google.ai.client.generativeai:generativeai:0.5.0")

}