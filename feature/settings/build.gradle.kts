plugins {
    alias(libs.plugins.cauri.android.feature)
}

android {
    namespace = "com.cauri.feature.settings"
}

dependencies {
    implementation(projects.core.capture)

    testImplementation(testFixtures(projects.core.capture))
}
