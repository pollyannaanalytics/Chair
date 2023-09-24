import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import java.util.regex.Pattern.compile

plugins {
    id("com.android.application")
    id("kotlin-android")
    id ("kotlin-kapt")
    id("com.google.protobuf")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
}
apply(plugin = "com.google.gms.google-services")


android {

    packagingOptions {
        exclude("META-INF/LICENSE")

        exclude("META-INF/INDEX.LIST")
    }
    compileSdkVersion(33)
    buildToolsVersion("33.0.1")
    namespace = "com.example.reclaim"

    defaultConfig {
        applicationId = "com.example.reclaim"
        minSdkVersion(21)
        targetSdkVersion(33)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures.dataBinding = true

    packagingOptions {
        exclude("META-INF/LICENSE")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/INDEX.LIST")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    val kotlinVersion = "1.8.20"
    val ktorVersion = "1.1.4"

    val room_version = "2.5.0"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")

    kapt("androidx.room:room-compiler:$room_version")


    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.9.0")

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    testImplementation("junit:junit:4.+")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.1")

    implementation("org.webrtc:google-webrtc:1.0.32006")

    implementation(platform("com.google.firebase:firebase-bom:29.3.1"))
    implementation ("com.google.firebase:firebase-firestore:24.8.1")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    //Ktor dependencies (you can retrofit instead)

    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-websocket:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")

    val nav_version = "2.5.3"
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("com.google.http-client:google-http-client:1.39.2")


// Moshi Codegen
    val version_moshi = "1.14.0"
    kapt ("com.squareup.moshi:moshi-kotlin-codegen:$version_moshi")

    // Retrofit with Moshi Converter

    val version_retrofit = "2.9.0"
    implementation ("com.squareup.retrofit2:retrofit:$version_retrofit")
    implementation ("com.squareup.retrofit2:converter-moshi:$version_retrofit")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation ("com.github.bumptech.glide:glide:4.15.1")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")


    implementation ("com.yuyakaido.android:card-stack-view:2.3.4")

    implementation ("com.google.android.material:material:1.9.0")

}
