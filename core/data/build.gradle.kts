plugins {
    alias(libs.plugins.cauri.android.library)
    alias(libs.plugins.cauri.android.hilt)
    alias(libs.plugins.cauri.android.room)
}

android {
    namespace = "com.cauri.core.data"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqlite.bundled.jvm)
}
