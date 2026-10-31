plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val syncWebAssets = tasks.register<Sync>("syncWebAssets") {
    val exportedOutDir = rootProject.file("../demo/out")
    from(exportedOutDir)
    into(layout.projectDirectory.dir("src/main/assets/locuterra"))
    onlyIf { exportedOutDir.exists() }
}

android {
    namespace = "org.umbruch.locuterra.pilot"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.umbruch.locuterra.pilot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-pilot"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.named("preBuild") {
    dependsOn(syncWebAssets)
}

dependencies {
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit4)
}
