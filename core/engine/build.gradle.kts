plugins {
    alias(libs.plugins.krawist.jvm.library)
}

dependencies {
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
