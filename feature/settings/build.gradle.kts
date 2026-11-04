plugins {
    alias(libs.plugins.cauri.android.feature)
}

android {
    namespace = "com.cauri.feature.settings"
}

dependencies {
    implementation(projects.core.capture)
    implementation(projects.core.data)
    implementation(projects.core.engine)
    implementation(libs.androidx.activity.compose)

    testImplementation(testFixtures(projects.core.capture))
    testImplementation(testFixtures(projects.core.data))
}
