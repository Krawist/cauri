plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.hilt)
    alias(libs.plugins.krawist.android.room)
}

android {
    namespace = "com.cauri.core.data"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqlite.bundled.jvm)
}
