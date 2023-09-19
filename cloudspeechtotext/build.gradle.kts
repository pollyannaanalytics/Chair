plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.cloudspeechtotext"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.6.0"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.cloud:google-cloud-speech:1.29.1")
    implementation ("com.google.auth:google-auth-library-oauth2-http:0.26.0"){
        exclude ("com.google.protobuf", "protobuf-java")
        exclude ("com.google.api.grpc", "proto-google-common-protos")
    }
    implementation ("io.grpc:grpc-okhttp:1.38.1"){
        exclude ("com.google.protobuf", "protobuf-java")
        exclude ("com.google.api.grpc", "proto-google-common-protos")
    }
    implementation ("io.grpc:grpc-stub:1.38.1"){
        exclude ("com.google.protobuf", "protobuf-java")
        exclude ("com.google.api.grpc", "proto-google-common-protos")
    }
    implementation ("com.google.api:gax:1.58.0"){
        exclude ("com.google.protobuf", "protobuf-java")
        exclude ("com.google.api.grpc", "proto-google-common-protos")
    }

    implementation ("com.google.dagger:dagger-android:2.35.1")
    annotationProcessor ("com.google.dagger:dagger-android-processor:2.21")
    annotationProcessor ("com.google.dagger:dagger-compiler:2.28.3")
}