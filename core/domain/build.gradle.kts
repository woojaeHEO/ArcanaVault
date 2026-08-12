plugins {
    alias(libs.plugins.holobinder.android.library)
}

android { namespace = "io.github.woojaeheo.holobinder.core.domain" }

dependencies {
    api(projects.core.model)
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
