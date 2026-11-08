import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.devtools.ksp)
}

// 비밀값은 추적하지 않는 local.properties 에서만 읽는다. 파일이나 키가 없어도 빌드가 통과해야 한다(빈 값).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun localProperty(key: String): String = localProperties.getProperty(key).orEmpty()

android {
    namespace = "com.offcourse.pickitup"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.offcourse.pickitup"
        // Pleos Connect 기준: API 34, minSdk 28 이상 (docs/PLEOS.md)
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PLEOS_CLIENT_ID", "\"${localProperty("pleos.client.id")}\"")
        buildConfigField("String", "PLEOS_CLIENT_SECRET", "\"${localProperty("pleos.client.secret")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.bundles.androidx)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.timber)
    implementation(libs.pleos.fused.location)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.test)
    debugImplementation(libs.bundles.debug)
}
