import java.util.zip.ZipFile

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
        versionCode = 4
        versionName = "0.2.1-pilot"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // Override aapt2 default (<dir>_*) so Next.js static export assets under
        // assets/locuterra/_next/ (CSS/JS chunks and build manifests) are packaged into the APK.
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
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

fun registerVerifyApkWebAssetsTask(variantName: String, apkDirPath: String) =
    tasks.register("verify${variantName.replaceFirstChar { it.uppercase() }}ApkWebAssets") {
        group = "verification"
        description = "Verifies that packaged $variantName APK contains Next.js _next CSS & JS chunks referenced by index.html."
        doLast {
            val apkDir = layout.buildDirectory.dir(apkDirPath).get().asFile
            val apkFile = apkDir.walkTopDown()
                .filter { it.isFile && it.extension == "apk" && !it.name.contains("androidTest") }
                .sortedBy { it.name.contains("unsigned") }
                .firstOrNull()
                ?: throw GradleException("No $variantName APK found under $apkDir")

            ZipFile(apkFile).use { zip ->
                val entries = zip.entries().asSequence().map { it.name }.toSet()
                val indexEntry = zip.getEntry("assets/locuterra/index.html")
                    ?: throw GradleException("${apkFile.name} is missing assets/locuterra/index.html")
                val cssChunks = entries.filter {
                    it.startsWith("assets/locuterra/_next/static/chunks/") && it.endsWith(".css")
                }
                val jsChunks = entries.filter {
                    it.startsWith("assets/locuterra/_next/static/chunks/") && it.endsWith(".js")
                }
                if (cssChunks.isEmpty()) {
                    throw GradleException(
                        "${apkFile.name} contains 0 Next.js CSS chunks under assets/locuterra/_next/static/chunks/ " +
                            "(check androidResources.ignoreAssetsPattern)."
                    )
                }
                if (jsChunks.isEmpty()) {
                    throw GradleException(
                        "${apkFile.name} contains 0 Next.js JS chunks under assets/locuterra/_next/static/chunks/ " +
                            "(check androidResources.ignoreAssetsPattern)."
                    )
                }

                val html = zip.getInputStream(indexEntry).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val refRegex = Regex("""(?:href|src)="(/locuterra/_next/[^"?#]+)"""")
                val refs = refRegex.findAll(html).map { it.groupValues[1] }.distinct().toList()
                if (refs.isEmpty()) {
                    throw GradleException("No /locuterra/_next/ asset references found in packaged index.html")
                }
                val missing = refs.filter { ref ->
                    val expectedEntry = "assets/" + ref.removePrefix("/")
                    expectedEntry !in entries
                }
                if (missing.isNotEmpty()) {
                    throw GradleException(
                        "${apkFile.name} is missing ${missing.size} _next assets referenced by index.html: $missing"
                    )
                }
                logger.lifecycle(
                    "Verified ${apkFile.name}: ${cssChunks.size} CSS chunk(s), ${jsChunks.size} JS chunk(s), " +
                        "${refs.size}/${refs.size} index.html _next references packaged."
                )
            }
        }
    }

val verifyDebugApkWebAssets = registerVerifyApkWebAssetsTask("debug", "outputs/apk/debug")
val verifyReleaseApkWebAssets = registerVerifyApkWebAssetsTask("release", "outputs/apk/release")

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy(verifyDebugApkWebAssets)
}
tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy(verifyReleaseApkWebAssets)
}

dependencies {
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit4)
}
