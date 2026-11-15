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

// 값을 따옴표로 감싸 적어도 BuildConfig 문자열이 깨지지 않게 벗긴다.
fun localProperty(key: String): String =
    localProperties.getProperty(key).orEmpty().trim().removeSurrounding("\"")

android {
    namespace = "com.offcourse.picko"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.offcourse.picko"
        // Pleos Connect 기준: API 34, minSdk 28 이상 (docs/PLEOS.md)
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"${localProperty("base.url")}\"")
        buildConfigField("String", "KAKAO_NATIVE", "\"${localProperty("kakao.native.key")}\"")
        // 시연 차량(정해 둔 좌표를 따라 이동). local.properties 의 demo.drive=true 나 -Pdemo.drive=true 로 켠다. 디버그 빌드에서만 쓰인다.
        val demoDrive = (findProperty("demo.drive") as String?) ?: localProperty("demo.drive")
        buildConfigField("boolean", "DEMO_DRIVE", (demoDrive == "true").toString())
        buildConfigField("String", "KAKAO_PLATFORM", "\"${localProperty("kakao.platform.key")}\"")
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

    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.okhttp)
    implementation(libs.bundles.retrofit)

    implementation(libs.timber)
    implementation(libs.pleos.fused.location)
    implementation(libs.pleos.speech.to.text)
    implementation(libs.pleos.text.to.speech)
    implementation(libs.pleos.navi.helper)
    implementation(libs.kakao.maps)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.test)
    debugImplementation(libs.bundles.debug)
}
