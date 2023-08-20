plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")
    kotlin("kapt")
}

kapt {
    correctErrorTypes = true
}

android {
    namespace = "com.svobnick.thisorthat"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.svobnick.thisorthat"

        versionCode = 5
        versionName = "1.3.0"

        minSdk = 27
        targetSdk = 33
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")

    // firebase
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-messaging:23.2.1")
    implementation("com.google.firebase:firebase-ads:22.2.0")
    implementation("com.google.firebase:firebase-analytics:21.3.0")
    implementation("com.google.firebase:firebase-analytics-ktx:21.3.0")
    implementation("com.google.firebase:firebase-crashlytics:18.4.0")

    // common android ui libraries
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.android.support:appcompat-v7:33.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // libraries for avatars
    implementation("com.squareup.picasso:picasso:2.71828")

    // application lifecycle
    val dagger_version = "2.47"
    implementation("com.google.dagger:dagger:$dagger_version")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    kapt("com.google.dagger:dagger-compiler:$dagger_version")

    val permissions_dispatcher_version = "4.9.2"
    implementation("com.github.permissions-dispatcher:permissionsdispatcher:$permissions_dispatcher_version")
    kapt("com.github.permissions-dispatcher:permissionsdispatcher-processor:$permissions_dispatcher_version")

    // mvp provider
    val moxy_version = "2.2.2"
    implementation("com.github.moxy-community:moxy:$moxy_version")
    implementation("com.github.moxy-community:moxy-app-compat:$moxy_version")
    kapt("com.github.moxy-community:moxy-compiler:$moxy_version")

    // reactive programming
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    // network
    implementation("com.android.volley:volley:1.2.1")

    testImplementation("junit:junit:4.13.2")
}
