plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.cauri.feature.insights"
}

dependencies {
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.data))
}
