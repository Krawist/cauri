plugins {
    alias(libs.plugins.cauri.android.feature)
}

android {
    namespace = "com.cauri.feature.onboarding"
}

dependencies {
    implementation(projects.core.data)
}
