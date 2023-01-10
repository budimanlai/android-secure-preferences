/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/25/20 10:01 PM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/25/20 9:26 PM
 */

plugins {
    id("com.android.application")
}

android {
    namespace = "com.budimanlai.demolib02"

    compileSdk = 33

    defaultConfig {
        applicationId = "com.budimanlai.demolib02"
        minSdk = 19
        targetSdk = 33
        versionCode = 2
        versionName = "1.2"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        named("release").configure {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(":securepreferences"))
}