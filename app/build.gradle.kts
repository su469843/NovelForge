plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "io.qzz.lstudy.novelforge"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.qzz.lstudy.novelforge"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 签名配置：通过环境变量驱动
    // 本地构建不设置这些变量时，release 包将不签名（unsigned）；
    // CI 中由 GitHub Actions 注入测试 keystore 后自动签名
    signingConfigs {
        val keystorePath = System.getenv("SIGNING_KEYSTORE_PATH")
        val keystorePwd = System.getenv("SIGNING_KEYSTORE_PASSWORD")
        val keyAlias = System.getenv("SIGNING_KEY_ALIAS")
        val keyPwd = System.getenv("SIGNING_KEY_PASSWORD")
        if (keystorePath != null && keystorePwd != null && keyAlias != null && keyPwd != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePwd
                this.keyAlias = keyAlias
                keyPassword = keyPwd
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 当存在 release 签名配置时启用签名，否则生成 unsigned 包
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Room schema 导出目录配置，便于后续数据库迁移校验
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // AndroidX 基础
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Room 数据库
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore 偏好存储
    implementation(libs.androidx.datastore.preferences)

    // Coroutines 协程
    implementation(libs.kotlinx.coroutines.android)

    // Hilt 依赖注入
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation Compose 导航
    implementation(libs.androidx.navigation.compose)

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
