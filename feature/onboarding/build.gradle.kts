plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.cauri.feature.onboarding"
}

dependencies {
    implementation(projects.core.capture)
    implementation(projects.core.data)
    implementation(libs.androidx.activity.compose)

    testImplementation(testFixtures(projects.core.capture))

    testImplementation(testFixtures(projects.core.data))
}
