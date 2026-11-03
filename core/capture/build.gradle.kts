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
    implementation(projects.core.design)
    implementation(projects.core.parsing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime)
    implementation(libs.play.services.location)
    ksp(libs.androidx.hilt.compiler)

    testFixturesImplementation(projects.core.data)
    testFixturesImplementation(libs.kotlinx.coroutines.core)

    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.kotlinx.coroutines.test)
}
