plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.swust.gameapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.swust.gameapplication"
        minSdk = 26
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    // Java 注解处理器（用于自动生成 DAO 实现等代码，必须）
    annotationProcessor("androidx.room:room-compiler:2.8.4")
}
