plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.cauri.feature.cash"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(testFixtures(projects.core.data))
}
