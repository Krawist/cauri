plugins {
    alias(libs.plugins.cauri.android.library)
    alias(libs.plugins.cauri.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cauri.core.capture"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
