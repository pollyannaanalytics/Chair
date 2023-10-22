plugins {
    id("com.android.application")
    id("kotlin-android")
    id ("kotlin-kapt")
    id("com.google.protobuf")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}



android {

    kapt{
        generateStubs = true
    }

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
    buildFeatures.viewBinding = true

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

    implementation("com.google.android.ads:mediation-test-suite:3.0.0")
    testImplementation("junit:junit:4.12")
    testImplementation("junit:junit:4.12")
    val kotlinVersion = "1.8.20"
    val ktorVersion = "1.1.4"

    val room_version = "2.5.0"

//    properties.load(rootProject.file("local.properties").inputStream())
//
//    buildConfigField("String", "OPEN_AI_KEY", ""${properties.getProperty("OPEN_AI_KEY")}"")
//

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")

    kapt("androidx.room:room-compiler:$room_version")

    testImplementation ("junit:junit:4.13")


    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha02")
    implementation("com.google.android.material:material:1.9.0")

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    testImplementation("junit:junit:4.+")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("org.webrtc:google-webrtc:1.0.32006")

    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation ("com.google.firebase:firebase-firestore:24.8.1")
    implementation ("com.google.firebase:firebase-database:20.2.2")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation ("com.google.firebase:firebase-auth:22.1.2")
    //Ktor dependencies (you can retrofit instead)

    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-websocket:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")

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

//    implementation ("com.google.android.material:material:1.9.0")

    implementation ("com.airbnb.android:lottie:6.1.0")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")


    // AndroidX Test - Instrumented testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")
//    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation("androidx.arch.core:core-testing:2.1.0")

    debugImplementation("androidx.fragment:fragment-testing:1.5.5")

    // Optional -- Mockito framework
    testImplementation ("org.mockito:mockito-core:5.6.0")
    // Optional -- mockito-kotlin
    testImplementation ("org.mockito.kotlin:mockito-kotlin:3.2.0")



}
