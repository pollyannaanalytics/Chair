

buildscript {

    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
        classpath( "androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}