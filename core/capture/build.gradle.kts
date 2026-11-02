plugins {
    alias(libs.plugins.cauri.android.library)
    alias(libs.plugins.cauri.android.hilt)
}

android {
    namespace = "com.cauri.core.capture"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
