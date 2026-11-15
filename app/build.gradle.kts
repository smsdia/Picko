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

// release 서명 정보. `-P`(CI 등)가 local.properties 보다 먼저다. 키스토어와 비밀번호는 저장소에 올리지 않는다.
//   release.store.file(선택, 기본 keystore/picko_release_key.jks), release.store.password, release.key.alias, release.key.password
fun releaseSigningProperty(key: String): String =
    (findProperty(key) as String?)?.trim().orEmpty().ifEmpty { localProperty(key) }

val releaseKeystoreFile: File = rootProject.file(
    releaseSigningProperty("release.store.file").ifEmpty { "keystore/picko_release_key.jks" },
)

// 키스토어 파일과 값이 모두 있을 때만 서명한다. 없으면 서명되지 않은 release APK가 만들어지고 빌드는 통과한다.
val hasReleaseSigning: Boolean = releaseKeystoreFile.exists() &&
    listOf("release.store.password", "release.key.alias", "release.key.password")
        .all { releaseSigningProperty(it).isNotEmpty() }

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
        // 시연 차량(정해 둔 좌표를 따라 이동). local.properties 의 demo.drive=true 나 -Pdemo.drive=true 로 켠다. 빌드 종류와 상관없이 이 값만 본다(-P가 local.properties보다 먼저다). 제출·배포용 빌드는 -Pdemo.drive=false 로 만든다.
        val demoDrive = (findProperty("demo.drive") as String?) ?: localProperty("demo.drive")
        buildConfigField("boolean", "DEMO_DRIVE", (demoDrive == "true").toString())
        buildConfigField("String", "KAKAO_PLATFORM", "\"${localProperty("kakao.platform.key")}\"")
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseSigningProperty("release.store.password")
                keyAlias = releaseSigningProperty("release.key.alias")
                keyPassword = releaseSigningProperty("release.key.password")
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")

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
