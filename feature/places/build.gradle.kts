plugins {
    alias(libs.plugins.cauri.android.feature)
}

android {
    namespace = "com.cauri.feature.places"
}

dependencies {
    implementation(projects.core.capture)
    implementation(projects.core.data)
    implementation(libs.androidx.activity.compose)

    testImplementation(testFixtures(projects.core.capture))
    testImplementation(testFixtures(projects.core.data))
}
