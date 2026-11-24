plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
}

android {
    namespace = "com.cauri.core.design"
}

dependencies {
    // Glass surfaces and backdrop of the kit, used by every screen.
    api("io.github.krawist:design")
}
