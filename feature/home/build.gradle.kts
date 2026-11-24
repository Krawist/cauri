plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.cauri.feature.home"
}

dependencies {
    implementation(projects.core.capture)
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.capture))
    testImplementation(testFixtures(projects.core.data))
}
