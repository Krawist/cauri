plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
}

android {
    namespace = "com.cauri.core.design"
}

dependencies {
    implementation(libs.haze)
    implementation(libs.haze.blur)
}
