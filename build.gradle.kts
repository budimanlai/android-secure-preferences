/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/25/20 10:32 PM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/25/20 9:26 PM
 */

buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}


tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}