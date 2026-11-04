plugins {
    alias(libs.plugins.cauri.android.feature)
}

android {
    namespace = "com.cauri.feature.transactions"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.ai))
    testImplementation(testFixtures(projects.core.data))
}
