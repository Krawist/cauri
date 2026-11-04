plugins {
    alias(libs.plugins.cauri.android.library)
    alias(libs.plugins.cauri.android.hilt)
}

android {
    namespace = "com.cauri.core.ai"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    api(projects.core.parsing)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mlkit.genai.prompt)
}
