plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}


object Libs {
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.7.0-alpha03"
    const val PLAY_SERVICES_AUTH = "com.google.android.gms:play-services-auth:20.7.0"
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics"
}

android {
    buildFeatures {
        viewBinding = true
        compose = true
    }

    namespace = "com.example.monopolydeal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.monopolydeal"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "xsd/catalog.xml"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/INDEX.LIST"
        }
        buildToolsVersion = "34.0.0"
        ndkVersion = "26.1.10909125"
    }

    dependencies {
        implementation ("com.android.support:cardview-v7:28.0.0")
        //noinspection UseTomlInstead
        implementation ("com.google.android.gms:play-services-basement:18.3.0")
        //noinspection UseTomlInstead
        implementation("androidx.fragment:fragment-ktx:1.6.2")
        //noinspection UseTomlInstead
        implementation("androidx.activity:activity-ktx:1.8.2")
        //noinspection UseTomlInstead
        implementation ("com.google.code.gson:gson:2.10.1")
        //noinspection UseTomlInstead
        implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
        //noinspection UseTomlInstead
        implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
        //noinspection UseTomlInstead
        implementation("com.google.firebase:firebase-auth:22.3.0")
        //noinspection UseTomlInstead
        implementation("com.google.firebase:firebase-database:20.3.0")
        //noinspection UseTomlInstead
        implementation("androidx.databinding:databinding-runtime:8.3.0-beta01")
        //noinspection UseTomlInstead
        implementation("com.google.android.gms:play-services-auth:20.7.0")
        //noinspection UseTomlInstead
        implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
        //noinspection UseTomlInstead
        implementation("com.google.firebase:firebase-analytics")
        implementation(Libs.PLAY_SERVICES_AUTH)
        implementation(Libs.ANDROIDX_APPCOMPAT)
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.constraintlayout)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        //noinspection UseTomlInstead
        debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.13")
    }
}
dependencies {
    implementation(libs.androidx.recyclerview)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}
