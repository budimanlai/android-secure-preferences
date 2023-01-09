plugins {
    id("com.android.library")
}

android {
    namespace = "com.budimanlai.securepreferences"

    compileSdk = 33

    defaultConfig {
        minSdk = 19
        targetSdk = 33
        versionCode = 5
        versionName = "1.5"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.preference:preference:1.2.0")
}