plugins {
    alias(libs.plugins.cauri.android.library)
    alias(libs.plugins.cauri.android.compose)
}

android {
    namespace = "com.cauri.core.design"
}

dependencies {
    implementation(libs.haze)
}
